#!/bin/sh
SRC_DIR=./target/generated-sources/javacc
TARGET_DIR=./target
clang++ -O2 -fbracket-depth=1024 -funsigned-char -I. -I./$SRC_DIR -o $TARGET_DIR/sqlparser -std=c++11 ParseErrorHandler.cc main.cc $SRC_DIR/*.cc unparser.cc
