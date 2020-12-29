# Concatenate all the fragments into a .jj file.
pwd
GRAMMAR_DIR='../grammar'
GEN_DIR='target/generated-sources/javacc'
mkdir -p $GEN_DIR
cat ./javacc-options.txt $GRAMMAR_DIR/kw.txt $GRAMMAR_DIR/sql-spec.txt $GRAMMAR_DIR/presto-extensions.txt $GRAMMAR_DIR/nonreservedwords.txt $GRAMMAR_DIR/lexical-elements.txt > $GEN_DIR/parser_tmp.jjt
