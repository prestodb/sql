#!/bin/bash

env | grep DIR
mkdir -p ${INSTALL_DIR}
cat \
    ${SRCDIR}/javacc-options.txt \
    ${SRCDIR}/kw.txt \
    ${SRCDIR}/sql-spec.txt \
    ${SRCDIR}/presto-extensions.txt \
    ${SRCDIR}/nonreservedwords.txt \
    ${SRCDIR}/lexical-elements.txt \
    > ${INSTALL_DIR}/parser_tmp.jjt
java -cp ${SRCDIR}/javacc/javacc.jar jjtree -OUTPUT_DIRECTORY=${INSTALL_DIR} ${INSTALL_DIR}/parser_tmp.jjt
java -cp ${SRCDIR}/javacc/javacc.jar javacc -OUTPUT_DIRECTORY=${INSTALL_DIR} ${INSTALL_DIR}/parser_tmp.jj
touch ../ready.txt
