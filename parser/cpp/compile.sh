#!/bin/sh
SRC_DIR=./target/generated-sources/javacc
TARGET_DIR=./target
clang++ -fbracket-depth=1024 -funsigned-char -I. -I./$SRC_DIR -o $TARGET_DIR/sqlparser -std=c++11 ParseErrorHandler.cc main.cc $SRC_DIR/*.cc
