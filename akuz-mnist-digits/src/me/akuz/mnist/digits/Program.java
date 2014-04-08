package me.akuz.mnist.digits;

import me.akuz.core.StringUtils;
import me.akuz.core.logs.LocalMonitor;
import me.akuz.core.logs.Monitor;
import me.akuz.core.logs.SystemOutMonitor;

public class Program {
    
	public static void main(String[] args) {
		
		String usageString = 
			"ARGUMENTS:\n" + 
			"   -trainFile string : File containing training data\n" +
			"   -outputDir string : Directory where to save output files\n"
			;

		String  trainFile = null;
		String  outputDir = null;
		
		try {
			
			if (args != null) {
				for (int i=0; i < args.length; i++) {
					
					if ("-trainFile".equals(args[i])) {
						if (i+1 < args.length) {
							trainFile = StringUtils.unquote(args[i+1]);
							i++;
						}
					} else if ("-outputDir".equals(args[i])) {
						if (i+1 < args.length) {
							outputDir = StringUtils.unquote(args[i+1]);
							i++;
						}
					}
				}
			}
	
			if (trainFile == null) {
				throw new IllegalArgumentException("Training data file not specified");
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
		
	    // configure monitor
		Monitor monitor = new SystemOutMonitor();
		Monitor programMonitor = new LocalMonitor(Program.class.getSimpleName(), monitor);

		// create program options
		ProgramOptions options = new ProgramOptions(
				trainFile,
				outputDir);
		
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
