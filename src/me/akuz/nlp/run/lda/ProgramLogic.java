package me.akuz.nlp.run.lda;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import Jama.Matrix;

import me.akuz.core.FileUtils;
import me.akuz.core.HashIndex;
import me.akuz.core.Hit;
import me.akuz.core.Index;
import me.akuz.core.StringUtils;
import me.akuz.core.logs.LogUtils;
import me.akuz.core.logs.ManualResetLogManager;
import me.akuz.core.math.MatrixUtils;
import me.akuz.nlp.corpus.Corpus;
import me.akuz.nlp.corpus.CorpusDoc;
import me.akuz.nlp.corpus.CorpusPlace;
import me.akuz.nlp.parse.RegexWordsParser;
import me.akuz.nlp.porter.PorterStemmer;
import me.akuz.nlp.porter.PorterStopWords;
import me.akuz.nlp.topics.LDAGibbs;
import me.akuz.nlp.topics.LDAGibbsAlpha;
import me.akuz.nlp.topics.LDAGibbsBeta;
import me.akuz.nlp.topics.LDAGibbsSave;
import me.akuz.nlp.topics.LDAGibbsTopic;
import me.akuz.nlp.topics.LDAGibbsUnstemmer;

public final class ProgramLogic {
	
	public ProgramLogic() {
	}

	public void execute(ProgramOptions options) throws Exception {
		
		final Logger log = LogUtils.getLogger(this.getClass().getName());

		log.info("Creating stemmer...");
		PorterStemmer porterStemmer = new PorterStemmer("_");

		Set<String> stopStems = null;
		if (options.getStopWordsFile() != null) {
			log.info("Loading stop words...");
			stopStems = PorterStopWords.loadStopWordsAndStemThem(porterStemmer, options.getStopWordsFile());
		} else {
			log.info("Stop words file not specified.");
		}
		
		log.info("Checking output dir...");
		FileUtils.isDirExistsOrCreate(options.getOutputDir());
		
		log.info("Creating words parser...");
		RegexWordsParser wordsParser = new RegexWordsParser(porterStemmer);
		
		log.info("Loading corpus...");
		Index<String> stemsIndex = new HashIndex<>();
		Index<String> wordsIndex = new HashIndex<>();
		Corpus corpus = new Corpus(stemsIndex, wordsIndex);
		List<File> inputFiles = FileUtils.getFiles(options.getInputDir());
		for (int i=0; i<inputFiles.size(); i++) {
			
			File file = inputFiles.get(i);
			if (file.getName().startsWith(".")) {
				continue;
			}
			if (file.getName().length() < 5) {
				continue;
			}
			if (!file.getName().substring(file.getName().length()-4).equalsIgnoreCase(".txt")) {
				continue;
			}

			String str = FileUtils.readEntireFile(file);
			Map<String, List<Hit>> hitsByStem = wordsParser.extractHitsByStem(str, new Hit(0, str.length()));
			
			if (hitsByStem != null && hitsByStem.size() > 0) {
				
				CorpusDoc doc = new CorpusDoc();
				
				for (Entry<String, List<Hit>> entry : hitsByStem.entrySet()) {
					
					String stem = entry.getKey();
					if (stopStems != null && stopStems.contains(stem)) {
						continue;
					}
					
					List<Hit> hits = entry.getValue();
					
					int stemIndex = stemsIndex.ensure(stem);
					for (int h=0; h<hits.size(); h++) {
						
						Hit hit = hits.get(h);
						String word = str.substring(hit.start(), hit.end());
						int wordIndex = wordsIndex.ensure(word);
						
						CorpusPlace place = new CorpusPlace(stemIndex, wordIndex);
						doc.addPlace(place);
					}
				}
				
				corpus.addDoc(doc);
			}
			
			if ((i+1) % 10 == 0) {
				log.info("Parsed " + (i+1) + "files.");
			}
		}
		
		log.info("Configuring topics...");
		List<LDAGibbsTopic> topics = new ArrayList<>();
		double normalTopicsCorpusFrac = 1.0;
		if (options.getNoiseTopicFraq() != null && 
			options.getNoiseTopicFraq() > 0) {
			normalTopicsCorpusFrac -= options.getNoiseTopicFraq();
			LDAGibbsTopic noiseTopic = new LDAGibbsTopic("noise", options.getNoiseTopicFraq());
			topics.add(noiseTopic);
			log.info("Added noise topic with corpus fraction " + options.getNoiseTopicFraq());
		}
		final double perTopicCorpusFrac = normalTopicsCorpusFrac / options.getThreadCount();
		for (int topicNumber = 1; topicNumber <= options.getTopicCount(); topicNumber++) {
			LDAGibbsTopic normalTopic = new LDAGibbsTopic("topic" + topicNumber, perTopicCorpusFrac);
			topics.add(normalTopic);
		}
		log.info("Added " + options.getTopicCount() + " topics with total corpus fraction " + normalTopicsCorpusFrac);
		LDAGibbsAlpha alpha = new LDAGibbsAlpha(corpus, topics, options.getDocMinTopicCount(), options.getDocLengthForExtraTopic());
		LDAGibbsBeta beta = new LDAGibbsBeta(corpus, topics);
		
		log.info("Configuring LDA...");
		Logger ldaLog = null;
		if (Level.FINEST.equals(options.getLogLevel())) {
			ldaLog = LogUtils.getLogger(LDAGibbs.class.getName());
		}
		final LDAGibbsUnstemmer unstemmer = new LDAGibbsUnstemmer(corpus);
		final LDAGibbs lda = new LDAGibbs(ldaLog, corpus, topics, alpha, beta, options.getThreadCount());

		System.out.println("Adding shutdown handler...");
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		    	try {
					log.info("Terminating LDA...");
					lda.terminate();
					log.info("LDA terminated.");
		    	} finally {
		    		ManualResetLogManager.resetFinally();
		    	}
		    }
		});
		
		log.fine("Burning in Gibbs sampler...");
		int iter = 1;
		double temperature = options.getBurnInStartTemp();
		while (temperature > options.getBurnInEndTemp()) {
			lda.setTemperature(temperature);
			iter = lda.run(iter, options.getBurnInTempIter());
			temperature *= options.getBurnInTempDecay();
		}
		
		log.fine("Sampling from Gibbs sampler...");
		Matrix mTopic = new Matrix(options.getTopicCount(), 1);
		Matrix mStemTopic = new Matrix(stemsIndex.size(), options.getTopicCount());
		lda.setTemperature(options.getBurnInEndTemp());
		for (int i=0; i<options.getSamplingIter(); i++) {
			iter = lda.run(iter, 1);
			lda.sampleTopic(mTopic);
			lda.sampleStemTopic(mStemTopic);
			unstemmer.sample();
		}
		unstemmer.optimize();
		
		log.fine("Saving results...");
		FileUtils.writeList(StringUtils.concatPath(options.getOutputDir(), "stems.txt"), stemsIndex.getList());
		MatrixUtils.writeMatrix(StringUtils.concatPath(options.getOutputDir(), "mTopic.csv"), mTopic);
		MatrixUtils.writeMatrix(StringUtils.concatPath(options.getOutputDir(), "mStemTopic.csv"), mStemTopic);
		new LDAGibbsSave(log, corpus, topics, mTopic, mStemTopic, unstemmer, options.getTopicOutputStemsCount(), 
				StringUtils.concatPath(options.getOutputDir(), "topics.txt"));
		
		log.fine("DONE.");
	}
}
