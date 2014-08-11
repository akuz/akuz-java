#!/bin/bash

echo START: akuz-java-mnist-digits...

java -Xms256m -Xmx4g \
        -Dfile.encoding=UTF-8 \
        -jar ./akuz-java-mnist-digits-0.0.2.jar \
        -trainFile ./input/train.csv \
        -outputDir ./output \

if [ "$?" -ne "0" ]; then
  echo "ERROR: akuz-java-mnist-digits"

  exit 1
else
  echo "DONE: akuz-java-mnist-digits"
  exit 0
fi
