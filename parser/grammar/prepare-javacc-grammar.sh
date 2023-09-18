# Concatenate all the fragments into a .jj file.
gendir='../target/generated-sources/javacc'
mkdir -p $gendir
cat javacc-options-java.txt nonreservedwords.txt reservedwords.txt sql-spec.txt presto-extensions.txt unicode-identifier-start.txt unicode-identifier-extend.txt ws.txt lexical-elements.txt > $gendir/parser_tmp.jjt
