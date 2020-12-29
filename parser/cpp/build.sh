#!/bin/sh

echo 'Generating parser ...'
mvn clean generate-sources
echo 'Compiling ...'
./compile.sh
