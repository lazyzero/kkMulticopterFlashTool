#!/bin/sh

cd $(dirname $0)
java -Djava.library.path=./lib:./lib/linux32 -jar ./lib/kkMulticopterFlashTool.jar
