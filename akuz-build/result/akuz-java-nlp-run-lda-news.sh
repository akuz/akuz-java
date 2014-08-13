#!/bin/bash

echo START: akuz-java-nlp-run-lda-news...

java -Xms256m -Xmx4g \
        -Dfile.encoding=UTF-8 \
        -jar ./bin/akuz-nlp-run-lda-0.0.3.jar \
        -inputDir /Users/andrey/SkyDrive/Documents/Data/news/news_1k \
        -outputDir ./output \
        -topicsConfigFile ./topics_config.txt \
        -stopWordsFile ./stop_words.txt \
		-burnInTempIter 25 \
		-samplingIter 250 \
        -threadCount 4 \

if [ "$?" -ne "0" ]; then
  echo "ERROR: akuz-java-nlp-run-lda-news"

  exit 1
else
  echo "DONE: akuz-java-nlp-run-lda-news"
  exit 0
fi
