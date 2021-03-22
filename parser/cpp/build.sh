#!/bin/sh

echo 'Generating parser ...'
mvn clean generate-sources
mv target/generated-sources/javacc/SqlParserVisitor.h .
echo 'Compiling ...'
./compile.sh
