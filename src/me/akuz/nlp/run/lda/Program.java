package me.akuz.nlp.run.lda;

import me.akuz.core.StringUtils;
import me.akuz.core.logs.LocalMonitor;
import me.akuz.core.logs.Monitor;
import me.akuz.core.logs.SystemOutMonitor;

public class Program {
    
	public static void main(String[] args) {
		
		String usageString = 
			"ARGUMENTS:\n" + 
			"   -inputDir string               : Directory containing input text files\n" +
			"   -outputDir string              : Directory where to save output files\n" +
			" [ -stopWordsFile string        ] : File with stop words to ignore (default none)\n" +
			" [ -topicCount int              ] : Number of topics for LDA inference (default 10)\n" +
			" [ -topicOutputStemsCount int   ] : Number of stems to output for each topic (default 100)\n" +
			" [ -noiseTopicProportion double ] : Noise topic proportion (default 0.5)\n" +
			" [ -threadCount int             ] : Number of threads to use (default 2)\n" +
			" [ -burnInStartTemp int         ] : Burn in start temperature (default 1.0)\n" +
			" [ -burnInEndTemp int           ] : Burn in end temperature (default 0.1)\n" +
			" [ -burnInTempDecay double      ] : Burn in temperature decay (default 0.75)\n" +
			" [ -burnInTempIter int          ] : Burn in iterations count per temperature (default 10)\n" +
			" [ -samplingIter int            ] : Sampling iterations count (default 100)\n";

		String  inputDir = null;
		String  outputDir = null;
		String  stopWordsFile = null;
		Integer topicCount = 10;
		Integer topicOutputStemsCount = 100;
		Double  noiseTopicProportion = 0.5;
		Integer threadCount = 2;
		Double  burnInStartTemp = 1.0;
		Double  burnInEndTemp = 0.1;
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
					} else if ("-noiseTopicProportion".equals(args[i])) {
						if (i+1 < args.length) {
							noiseTopicProportion = Double.parseDouble(StringUtils.unquote(args[i+1]));
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
			if (noiseTopicProportion != null && (noiseTopicProportion < 0 || noiseTopicProportion >= 1)) {
				throw new IllegalArgumentException("Argument noiseTopicProportion must be within interval [0, 1)");
			}


		} catch (Exception e) {
			
			System.out.println("******** Arguments Error ********");
			System.out.println(e.toString());
			System.out.println("******** Correct Usage ********");
			System.out.println(usageString);
			return;
		}
		
	    // configure monitor
		Monitor monitor = new SystemOutMonitor();
		Monitor programMonitor = new LocalMonitor(Program.class.getSimpleName(), monitor);

		// create program options
		ProgramOptions options = new ProgramOptions(
				inputDir,
				outputDir,
				topicCount,
				topicOutputStemsCount,
				noiseTopicProportion,
				stopWordsFile,
				threadCount,
				burnInStartTemp,
				burnInEndTemp,
				burnInTempDecay,
				burnInTempIter,
				samplingIter);
		
		programMonitor.write("OPTIONS: \n" + options);
		
		programMonitor.write("STARTING...");
		ProgramLogic logic = new ProgramLogic();
		try {
			logic.execute(monitor, options);
		} catch (Exception ex) {
			programMonitor.write("Unhandled exception", ex);
		}
		programMonitor.write("DONE.");
	}

}
