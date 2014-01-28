#!/bin/bash

echo START: akuz-java-nlp-run-lda...

java -Xms256m -Xmx4g \
        -Dfile.encoding=UTF-8 \
        -jar ./akuz-java-nlp-run-lda-0.0.1.jar \
        -inputDir ./input/news_10k \
        -outputDir ./output \
        -topicsConfigFile ./topics_config.txt \
        -stopWordsFile ./stop_words.txt \
	-burnInTempIter 50 \
	-samplingIter 500 \
        -threadCount 4 \

if [ "$?" -ne "0" ]; then
  echo "ERROR: akuz-java-nlp-run-lda"

  exit 1
else
  echo "DONE: akuz-java-nlp-run-lda"
  exit 0
fi
