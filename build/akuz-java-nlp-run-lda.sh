#!/bin/bash

echo START: akuz-java-nlp-run-lda...

java -Xms256m -Xmx4g \
        -Dfile.encoding=UTF-8 \
        -jar ./akuz-java-nlp-run-lda-0.0.1.jar \
<<<<<<< HEAD
        --inputDir ./input/news10k \
=======
        -inputDir ./input/news_10k \
>>>>>>> f7e38be02f86edd2504e793817205110198d78f0
        -outputDir ./output \
        -topicsConfigFile ./topics_config.txt \
        -stopWordsFile ./stop_words.txt \
        -threadCount 4 \

if [ "$?" -ne "0" ]; then
  echo "ERROR: akuz-java-nlp-run-lda"

  exit 1
else
  echo "DONE: akuz-java-nlp-run-lda"
  exit 0
fi
