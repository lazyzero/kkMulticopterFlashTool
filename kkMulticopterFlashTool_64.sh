#!/bin/sh

cd $(dirname $0)
java -Djava.library.path=./lib:./lib/linux64 -jar ./lib/kkMulticopterFlashTool.jar
