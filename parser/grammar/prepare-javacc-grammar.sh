# Concatenate all the fragments into a .jj file.
gendir='../main/jjtree/com/facebook/coresql/parser/'
mkdir -p $gendir
cat javacc-options-java.txt nonreservedwords.txt reservedwords.txt sql-spec.txt presto-extensions.txt lexical-elements.txt > $gendir/parser.jjt
