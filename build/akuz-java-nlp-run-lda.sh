#!/bin/bash

echo START: akuz-java-nlp-run-lda...

java -Xms256m -Xmx4g \
        -Dfile.encoding=UTF-8 \
        -jar ./akuz-java-nlp-run-lda-0.0.1.jar \
        -inputDir ./input/news10k \
        -outputDir ./output \
        -stopWordsFile ./stop_words.txt \
        -topicCount 20 \
        -threadCount 4 \

if [ "$?" -ne "0" ]; then
  echo "ERROR: akuz-java-nlp-run-lda"

  exit 1
else
  echo "DONE: akuz-java-nlp-run-lda"
  exit 0
fi
