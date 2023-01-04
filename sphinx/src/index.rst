##############################
Presto SQL:2016 Parser Library
##############################

.. toctree::
   :maxdepth: 2
   :hidden:

   usage
   contribution
   syntax
   keywords
   changelog

.. image:: https://maven-badges.herokuapp.com/maven-central/com.facebook.presto/presto-coresql/badge.svg
    :alt: Maven Repo

.. image:: https://github.com/prestodb/sql/actions/workflows/maven.yml/badge.svg
    :alt: Maven Build Status

.. image:: https://www.javadoc.io/badge/com.facebook.presto/presto-coresql.svg
    :alt: Java Docs

**Presto SQL:2016 Parser** is a SQL statement parser built from JavaCC. It translates SQLs in a traversable hierarchy of Java classes.

Latest stable release: |PRESTO_SQL_PARSER_STABLE_VERSION_LINK|

Development version: |PRESTO_SQL_PARSER_SNAPSHOT_VERSION_LINK|

******************************
SQL Dialects
******************************

**Presto SQL:2016 Parser** is SQL:2016 compliant and provides support various RDBMS such as:

    * Oracle Database
    * MS SqlServer
    * MySQL and MariaDB
    * PostgreSQL
    * H2

Although support for specific dialects is work in progress and has not been implemented yet.

*******************************
Features
*******************************

    * Comprehensive support for statements:
        - QUERY: ``SELECT ...``
        - DML: ``INSERT ... INTO ...`` ``UPDATE ...`` ``MERGE ... INTO ...`` ``DELETE ... FROM ...``
        - DDL: ``CREATE ...`` ``ALTER ...`` ``DROP ...``

    * Nested Expressions (e.g. Sub-Selects)
    * ``WITH`` clauses
    * Un-Parser for a Statement AST Node
    * Can generate both Java and C++ Parser/Unparser






