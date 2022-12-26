******************************
How to use it
******************************

Compile from Source Code
==============================

You will need to have ``JDK 8`` or ``JDK 11`` installed.

.. tabs::

  .. tab:: Maven

    .. code-block:: shell

			git clone https://github.com/prestodb/sql.git
			cd sql	
			mvn install

  .. tab:: Gradle

	  .. code-block:: shell
    
			git clone https://github.com/prestodb/sql.git
			cd sql
			gradle build



Maven Artifacts
==============================

.. tabs::
	

  .. tab:: Stable Release

		.. code-block:: xml
			:substitutions:

			<dependency>
				<groupId>com.facebook.presto</groupId>
				<artifactId>presto-coresql</artifactId>
				<version>|JSQLPARSER_VERSION|</version>
			</dependency>

  .. tab:: Development Snapshot
		
		.. code-block:: xml
			:substitutions:			
 
			<repositories>
				<repository>
					<id>presto-coresql-snapshots</id>
					<snapshots>
						<enabled>true</enabled>
					</snapshots>
					<url>https://oss.sonatype.org/content/groups/public/</url>
				</repository>
			</repositories> 
			<dependency>
				<groupId>com.facebook.presto</groupId>
				<artifactId>presto-coresql</artifactId>
				<version>|JSQLPARSER_SNAPSHOT_VERSION|</version>
			</dependency>

			
Parse a SQL Statements
==============================			

Parse the SQL Text into Java Objects:

.. code-block:: java

        import com.facebook.coresql.parser.AstNode;
        import com.facebook.coresql.parser.ParserHelper;
        import com.facebook.coresql.parser.Unparser;

        String sqlStr = "select 1 from dual where a=b";
        AstNode ast = ParserHelper.parseStatement(sqlStr);
        String unparsedSqlStr = Unparser.unparse(ast);


