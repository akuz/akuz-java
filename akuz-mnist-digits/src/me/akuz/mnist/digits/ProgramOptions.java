package me.akuz.mnist.digits;

import me.akuz.core.gson.GsonObj;

public final class ProgramOptions extends GsonObj {

	private static final String _trainFile = "trainFile";
	private static final String _outputDir = "outputDir";
	
	public ProgramOptions(
			String trainFile,
			String outputDir) {
		
		set(_trainFile, trainFile);
		set(_outputDir, outputDir);
	}
	
	public String getTrainFile() {
		return getString(_trainFile);
	}
	public String getOutputDir() {
		return getString(_outputDir);
	}

}
