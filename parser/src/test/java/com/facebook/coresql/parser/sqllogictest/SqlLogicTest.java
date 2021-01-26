/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.coresql.parser.sqllogictest;

import com.facebook.coresql.parser.AstNode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;

import static com.facebook.coresql.parser.ParserHelper.parseStatement;
import static com.facebook.coresql.parser.Unparser.unparse;
import static com.facebook.coresql.parser.sqllogictest.SqlLogicTestError.PARSING_ERROR;
import static java.nio.file.FileVisitResult.CONTINUE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public final class SqlLogicTest
{
    private static BufferedWriter writer;
    private static FileVisitor<Path> fileVisitor;
    private static Path startingDirectory;
    private static int maxFilesToVisit;
    private static int numFilesVisited;
    private static int numStatementsParsed;
    private static int numStatementsFound;

    private SqlLogicTest() {}

    private static void setup()
            throws IOException
    {
        writer = new BufferedWriter(new FileWriter("src/test/java/com/facebook/coresql/parser/sqllogictest/sqllogictest_results.txt"));
        startingDirectory = Paths.get("src/test/java/com/facebook/coresql/parser/sqllogictest/sqllogictest");
        fileVisitor = new SqlLogicTestFileVisitor();
        maxFilesToVisit = 10; // Note: visiting all files takes ~20 minutes on a Macbook Pro, i9, 32GB RAM
    }

    private static void shutdown()
            throws IOException
    {
        writer.write(
                "===============================================\n" +
                        "RESULTS\n" +
                        "===============================================\n");
        writer.write(String.format("%d statements found, %d statements successfully parsed.", numStatementsFound, numStatementsParsed));
        writer.write("\n");
        writer.write(String.format("%s%% of sqllogictest statements were successfully parsed.",
                new DecimalFormat("#.##").format(numStatementsParsed / (double) numStatementsFound * 100)));
        writer.close();
    }

    private static AstNode parse(String sql)
    {
        return parseStatement(sql);
    }

    private static void validateParsingOfSqlStatement(String expr)
            throws IOException
    {
        String errorType = "";
        AstNode ast = null;
        try {
            ast = parse(expr);
            assertNotNull(ast);
        }
        catch (AssertionError e) {
            errorType = PARSING_ERROR.toString();
        }

        String unparsed = "";
        try {
            unparsed = unparse(ast);
        }
        catch (Exception e) {
            errorType = PARSING_ERROR.toString();
        }

        try {
            assertEquals(expr.trim(), unparsed.trim());
        }
        catch (AssertionError e) {
            errorType = PARSING_ERROR.toString();
        }
        if (errorType.equals("")) {
            numStatementsParsed += 1;
        }
        else {
            writer.write(expr + " ----> " + errorType);
            writer.write("\n\n");
        }
    }

    public static void run()
            throws IOException
    {
        setup();
        Files.walkFileTree(startingDirectory, fileVisitor);
        shutdown();
    }

    private static class SqlLogicTestFileVisitor
            implements FileVisitor<Path>
    {
        private void processStatement(BufferedReader reader)
                throws IOException
        {
            StringBuilder statement = new StringBuilder();
            while (true) {
                final String line = reader.readLine();
                if (line == null || isLineAfterStatement(line)) {
                    break;
                }
                statement.append(line);
            }
            if (!statement.substring(statement.length() - 1).equals(";")) {
                statement.append(";"); // NOTE: This semi-colon is necessary for parsing. Failing to add it will add a very annoying bug.
            }
            numStatementsFound += 1;
            String sqlStatement = statement.toString();
            validateParsingOfSqlStatement(sqlStatement);
        }

        private static boolean isLineBeforeStatement(String line)
        {
            return line.startsWith("statement ok") || line.startsWith("query");
        }

        private static boolean statementIsAcceptableToAllRdms(String lineBeforeStatement)
        {
            return !(lineBeforeStatement.startsWith("onlyif") || lineBeforeStatement.startsWith("skipif"));
        }

        private static boolean isLineAfterStatement(String line)
        {
            return line.equals("----") || line.equals("");
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
        {
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException
        {
            if (Files.isHidden(file) || numFilesVisited > maxFilesToVisit) {
                return CONTINUE;
            }
            try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
                while (true) {
                    final String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    if (isLineBeforeStatement(line) && statementIsAcceptableToAllRdms(line)) {
                        processStatement(reader);
                    }
                }
            }
            numFilesVisited += 1;
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc)
                throws IOException
        {
            System.out.println("Failed to access file: " + file.toString());
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                throws IOException
        {
            return CONTINUE;
        }
    }
}
