package me.akuz.nlp.run.lda;

import java.util.logging.Level;

import me.akuz.core.Dbo;
import me.akuz.core.gson.GsonSerializers;

public final class ProgramOptions extends Dbo {

	private static final String _inputDir               = "inputDir";
	private static final String _outputDir              = "outputDir";
	private static final String _topicCount             = "topicCount";
	private static final String _topicOutputStemsCount  = "topicOutputStemsCount";
	private static final String _noiseTopicFrac         = "noiseTopicFrac";
	private static final String _docMinTopicCount       = "docMinTopicCount";
	private static final String _docLengthForExtraTopic = "docLengthForExtraTopic";
	private static final String _stopWordsFile          = "stopWordsFile";
	private static final String _threadCount            = "threadCount";
	private static final String _burnInStartTemp        = "burnInStartTemp";
	private static final String _burnInEndTemp          = "burnInEndTemp";
	private static final String _burnInTempDecay        = "burnInTempDecay";
	private static final String _burnInTempIter         = "burnInTempIter";
	private static final String _samplingIter           = "samplingIter";
	private static final String _logLevel               = "logLevel";
	
	public ProgramOptions(
			String inputDir,
			String outputDir,
			Integer topicCount,
			Integer topicOutputStemsCount,
			Double noiseTopicFrac,
			Integer docMinTopicCount,
			Integer docLengthForExtraTopic,
			String stopWordsFile,
			Integer threadCount,
			Double burnInStartTemp,
			Double burnInEndTemp,
			Double burnInTempDecay,
			Integer burnInTempIter,
			Integer samplingIter,
			Level logLevel) {
		
		set(_inputDir, inputDir);
		set(_outputDir, outputDir);
		set(_topicCount, topicCount);
		set(_topicOutputStemsCount, topicOutputStemsCount);
		set(_noiseTopicFrac, noiseTopicFrac);
		set(_docMinTopicCount, docMinTopicCount);
		set(_docLengthForExtraTopic, docLengthForExtraTopic);
		set(_stopWordsFile, stopWordsFile);
		set(_threadCount, threadCount);
		set(_burnInStartTemp, burnInStartTemp);
		set(_burnInEndTemp, burnInEndTemp);
		set(_burnInTempDecay, burnInTempDecay);
		set(_burnInTempIter, burnInTempIter);
		set(_samplingIter, samplingIter);
		set(_logLevel, logLevel);
	}
	
	public String getInputDir() {
		return getString(_inputDir);
	}
	public String getOutputDir() {
		return getString(_outputDir);
	}
	public Integer getTopicCount() {
		return getInteger(_topicCount);
	}
	public Integer getTopicOutputStemsCount() {
		return getInteger(_topicOutputStemsCount);
	}
	public Double getNoiseTopicFraq() {
		return getDouble(_noiseTopicFrac);
	}
	public Integer getDocMinTopicCount() {
		return getInteger(_docMinTopicCount);
	}
	public Integer getDocLengthForExtraTopic() {
		return getInteger(_docLengthForExtraTopic);
	}
	public String getStopWordsFile() {
		return getString(_stopWordsFile);
	}
	public Integer getThreadCount() {
		return getInteger(_threadCount);
	}
	public Double getBurnInStartTemp() {
		return getDouble(_burnInStartTemp);
	}
	public Double getBurnInEndTemp() {
		return getDouble(_burnInEndTemp);
	}
	public Double getBurnInTempDecay() {
		return getDouble(_burnInTempDecay);
	}
	public Integer getBurnInTempIter() {
		return getInteger(_burnInTempIter);
	}
	public Integer getSamplingIter() {
		return getInteger(_samplingIter);
	}
	public Level getLogLevel() {
		return getLogLevel(_logLevel);
	}
	
	@Override
	public String toString() {
		return GsonSerializers.NoHtmlEscapingPretty.toJson(getMap());
	}
}
