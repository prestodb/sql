#!/bin/sh

echo 'Generating parser ...'
mvn clean generate-sources
echo 'Compiloing ...'
./compile.sh
