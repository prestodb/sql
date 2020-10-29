# Concatenate all the fragments into a .jj file.
gendir='../target/generated-sources/javacc'
mkdir -p $gendir
cat javacc-options-java.txt kw.txt sql-spec.txt presto-extensions.txt nonreservedwords.txt lexical-elements.txt > $gendir/parser_tmp.jjt
