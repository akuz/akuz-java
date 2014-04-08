#!/bin/bash

echo START: akuz-java-nlp-run-lda...

java -Xms256m -Xmx4g \
        -Dfile.encoding=UTF-8 \
        -jar ./akuz-java-nlp-run-lda-0.0.2.jar \
        -inputDir /Users/andrey/Desktop/Company_descriptions \
        -outputDir /Users/andrey/Desktop/Company_desriptions_topics \
        -topicsConfigFile ./topics_config.txt \
        -stopWordsFile ./stop_words.txt \
		-burnInTempIter 50 \
		-samplingIter 1000 \
        -threadCount 4 \

if [ "$?" -ne "0" ]; then
  echo "ERROR: akuz-java-nlp-run-lda"

  exit 1
else
  echo "DONE: akuz-java-nlp-run-lda"
  exit 0
fi
