package me.akuz.nlp.run.lda;

import java.util.logging.Level;
import java.util.logging.Logger;

import me.akuz.core.StringUtils;
import me.akuz.core.logs.LogUtils;

public class Program {
    
	public static void main(String[] args) {
		
		String usageString = 
			"ARGUMENTS:\n" + 
			"   -inputDir string              : Directory containing input text files\n" +
			"   -outputDir string             : Directory where to save output files\n" +
			" [ -stopWordsFile string       ] : File with stop words to ignore (default none)\n" +
			" [ -topicCount int             ] : Number of topics for LDA inference (default 10)\n" +
			" [ -topicOutputStemsCount int  ] : Number of stems to output for each topic (default 100)\n" +
			" [ -noiseTopicFrac double      ] : Fraction of corpus for noise topic (default 0.5)\n" +
			" [ -docMinTopicCount int       ] : Assumed minimum topics per document (default 2)\n" +
			" [ -docLengthForExtraTopic int ] : Document length to assume a first extra topic (default 100)\n" +
			" [ -threadCount int            ] : Number of threads to use (default 2)\n" +
			" [ -burnInStartTemp int        ] : Burn in start temperature (default 1.0)\n" +
			" [ -burnInEndTemp int          ] : Burn in end temperature (default 0.05)\n" +
			" [ -burnInTempDecay double     ] : Burn in temperature decay (default 0.75)\n" +
			" [ -burnInTempIter int         ] : Burn in iterations count per temperature (default 10)\n" +
			" [ -samplingIter int           ] : Sampling iterations count (default 100)\n" +
			" [ -logLevel                   ] : Java logging level (default INFO)\n";

		String  inputDir = null;
		String  outputDir = null;
		String  stopWordsFile = null;
		Integer topicCount = 10;
		Integer topicOutputStemsCount = 100;
		Double  noiseTopicFrac = 0.5;
		Integer docMinTopicCount = 2;
		Integer docLengthForExtraTopic = 100;
		Integer threadCount = 2;
		Double  burnInStartTemp = 1.0;
		Double  burnInEndTemp = 0.05;
		Double  burnInTempDecay = 0.75;
		Integer burnInTempIter = 10;
		Integer samplingIter = 100;
		
		try {
			
			if (args != null) {
				for (int i=0; i < args.length; i++) {
					
					if ("-inputDir".equals(args[i])) {
						if (i+1 < args.length) {
							inputDir = StringUtils.unquote(args[i+1]);
							i++;
						}
					} else if ("-outputDir".equals(args[i])) {
						if (i+1 < args.length) {
							outputDir = StringUtils.unquote(args[i+1]);
							i++;
						}
					} else if ("-stopWordsFile".equals(args[i])) {
						if (i+1 < args.length) {
							stopWordsFile = StringUtils.unquote(args[i+1]);
							i++;
						}
					} else if ("-topicCount".equals(args[i])) {
						if (i+1 < args.length) {
							topicCount = Integer.parseInt(StringUtils.unquote(args[i+1]));
							i++;
						}
					} else if ("-topicOutputStemsCount".equals(args[i])) {
						if (i+1 < args.length) {
							topicOutputStemsCount = Integer.parseInt(StringUtils.unquote(args[i+1]));
							i++;
						}
					} else if ("-noiseTopicFrac".equals(args[i])) {
						if (i+1 < args.length) {
							noiseTopicFrac = Double.parseDouble(StringUtils.unquote(args[i+1]));
							i++;
						}
					} else if ("-docMinTopicCount".equals(args[i])) {
						if (i+1 < args.length) {
							docMinTopicCount = Integer.parseInt(StringUtils.unquote(args[i+1]));
							i++;
						}
					} else if ("-docLengthForExtraTopic".equals(args[i])) {
						if (i+1 < args.length) {
							docLengthForExtraTopic = Integer.parseInt(StringUtils.unquote(args[i+1]));
							i++;
						}
					} else if ("-threadCount".equals(args[i])) {
						if (i+1 < args.length) {
							threadCount = Integer.parseInt(StringUtils.unquote(args[i+1]));
							i++;
						}
					} else if ("-burnInStartTemp".equals(args[i])) {
						if (i+1 < args.length) {
							burnInStartTemp = Double.parseDouble(StringUtils.unquote(args[i+1]));
							i++;
						}
					} else if ("-burnInEndTemp".equals(args[i])) {
						if (i+1 < args.length) {
							burnInEndTemp = Double.parseDouble(StringUtils.unquote(args[i+1]));
							i++;
						}
					} else if ("-burnInTempDecay".equals(args[i])) {
						if (i+1 < args.length) {
							burnInTempDecay = Double.parseDouble(StringUtils.unquote(args[i+1]));
							i++;
						}
					} else if ("-burnInTempIter".equals(args[i])) {
						if (i+1 < args.length) {
							burnInTempIter = Integer.parseInt(StringUtils.unquote(args[i+1]));
							i++;
						}
					} else if ("-samplingIter".equals(args[i])) {
						if (i+1 < args.length) {
							samplingIter = Integer.parseInt(StringUtils.unquote(args[i+1]));
							i++;
						}
					}
				}
			}
	
			if (inputDir == null) {
				throw new IllegalArgumentException("Input directory not specified");
			}
			if (outputDir == null) {
				throw new IllegalArgumentException("Output directory not specified");
			}


		} catch (Exception e) {
			
			System.out.println("******** Arguments Error ********");
			System.out.println(e.toString());
			System.out.println("******** Correct Usage ********");
			System.out.println(usageString);
			return;
		}
		
	    // configure logging
		LogUtils.configure(Level.FINEST);
		Logger log = LogUtils.getLogger(Program.class.getName());

		// create program options
		ProgramOptions options = new ProgramOptions(
				inputDir,
				outputDir,
				topicCount,
				topicOutputStemsCount,
				noiseTopicFrac,
				docMinTopicCount,
				docLengthForExtraTopic,
				stopWordsFile,
				threadCount,
				burnInStartTemp,
				burnInEndTemp,
				burnInTempDecay,
				burnInTempIter,
				samplingIter);
		
		log.info("OPTIONS: \n" + options);
		
		log.info("STARTING...");
		ProgramLogic logic = new ProgramLogic();
		try {
			logic.execute(options);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Unhandled exception", ex);
		}
		log.info("DONE.");
	}

}
