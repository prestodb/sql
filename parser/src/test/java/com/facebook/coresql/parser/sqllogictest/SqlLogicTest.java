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
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import static com.facebook.coresql.parser.ParserHelper.parseStatement;
import static com.facebook.coresql.parser.Unparser.unparse;
import static java.nio.file.FileVisitResult.CONTINUE;

public final class SqlLogicTest
{
    private static AcceptedRdms currRdms;
    private static Set<AcceptedRdms> skipIfSet;
    private static AcceptedRdms onlyIf;
    private static BufferedWriter writer;
    private static FileVisitor<Path> fileVisitor;
    private static Path startingDirectory;
    private static int maxFilesToVisit;
    private static int numFilesVisited;
    private static int numStatementsParsed;
    private static int numStatementsFound;

    private enum SqlLogicTestError
    {
        PARSING_ERROR,
        UNPARSING_ERROR,
        UNPARSED_DOES_NOT_MATCH_ERROR
    }

    private enum AcceptedRdms
    {
        ALL,
        ORACLE,
        MYSQL,
        MSSQL,
        SQLITE,
        POSTGRESQL
    }

    private SqlLogicTest() {}

    public static void run()
            throws IOException
    {
        setup("all");
        Files.walkFileTree(startingDirectory, fileVisitor);
        shutdown();
    }

    public static void run(String rdms)
            throws IOException
    {
        setup(rdms);
        Files.walkFileTree(startingDirectory, fileVisitor);
        shutdown();
    }

    private static void setup(String rdms)
            throws IOException
    {
        verifyAndSetRdms(rdms);
        createFileForResults();
        writer = new BufferedWriter(new FileWriter("src/test/java/com/facebook/coresql/parser/sqllogictest/sqllogictest_results.txt"));
        startingDirectory = Paths.get("src/test/java/com/facebook/coresql/parser/sqllogictest/test_statements");
        fileVisitor = new SqlLogicTestFileVisitor();
        maxFilesToVisit = 15; // Note: visiting all files takes ~20 minutes on a Macbook Pro, i9, 32GB RAM
        skipIfSet = new HashSet<>();
        onlyIf = null;
    }

    private static void shutdown()
            throws IOException
    {
        writer.write(
                "===============================================\n" +
                        "RESULTS\n" +
                        "===============================================\n");
        writer.write(String.format("RDMS tested against: %s\n", currRdms));
        writer.write(String.format("%d statements found, %d statements successfully parsed.\n", numStatementsFound, numStatementsParsed));
        writer.write(String.format("%s%% of sqllogictest statements were successfully parsed.",
                new DecimalFormat("#.##").format(numStatementsParsed / (double) numStatementsFound * 100)));
        writer.close();
    }

    private static void verifyAndSetRdms(String rdms)
    {
        for (AcceptedRdms acceptedRdms : AcceptedRdms.values()) {
            if (acceptedRdms.toString().equalsIgnoreCase(rdms)) {
                currRdms = acceptedRdms;
                return;
            }
        }
        throw new IllegalArgumentException("An unsupported RDMS was given.");
    }

    private static void createFileForResults()
    {
        try {
            Files.createFile(Paths.get("src/test/java/com/facebook/coresql/parser/sqllogictest/sqllogictest_results.txt"));
        }
        catch (IOException e) {
            if (e.getClass() == FileAlreadyExistsException.class) {
                System.out.println("Results file already exists. We'll overwrite it with the new results.");
            }
            else {
                System.out.println("ERROR: An IOException occurred when trying to create the sqllogictest results file.");
                e.printStackTrace();
            }
        }
    }

    private static AstNode parse(String sql)
    {
        return parseStatement(sql);
    }

    private static void validateParsingOfSqlStatement(String expr)
            throws IOException
    {
        String errorType = "";
        try {
            errorType = SqlLogicTestError.PARSING_ERROR.toString();
            AstNode ast = parse(expr);
            if (ast == null) {
                throw new Exception();
            }
            errorType = SqlLogicTestError.UNPARSING_ERROR.toString();
            String unparsed = unparse(ast);
            errorType = SqlLogicTestError.UNPARSED_DOES_NOT_MATCH_ERROR.toString();
            if (!expr.trim().equals(unparsed.trim())) {
                throw new Exception();
            }
            errorType = "";
        }
        catch (Exception ignored) {
        }

        if (errorType.equals("")) {
            numStatementsParsed += 1;
        }
        else {
            writer.write(expr + " ----> " + errorType);
            writer.write("\n\n");
        }
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
                statement.append(";"); // NOTE: This semi-colon is necessary for parsing.
            }
            numStatementsFound += 1;
            validateParsingOfSqlStatement(statement.toString());
        }

        private static boolean isLineBeforeStatement(String line)
        {
            return line.startsWith("statement ok") || line.startsWith("query");
        }

        private static boolean statementIsAcceptableToCurrRdms()
        {
            if (currRdms == AcceptedRdms.ALL) {
                return skipIfSet.isEmpty() && onlyIf == null;
            }

            if (onlyIf != null && onlyIf != currRdms) {
                return false;
            }

            return !skipIfSet.contains(currRdms);
        }

        private static boolean isLineAfterStatement(String line)
        {
            return line.equals("----") || line.equals("");
        }

        private static boolean processConditionalRecord(String line)
        {
            if (line.startsWith("onlyif")) {
                String onlyIfRdms = line.split(" ")[1];
                for (AcceptedRdms acceptedRdms : AcceptedRdms.values()) {
                    if (acceptedRdms.toString().equalsIgnoreCase(onlyIfRdms)) {
                        onlyIf = acceptedRdms;
                    }
                }
                return true;
            }

            if (line.startsWith("skipif")) {
                String skipIfRdms = line.split(" ")[1];
                for (AcceptedRdms acceptedRdms : AcceptedRdms.values()) {
                    if (acceptedRdms.toString().equalsIgnoreCase(skipIfRdms)) {
                        skipIfSet.add(acceptedRdms);
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
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
                    if (processConditionalRecord(line)) {
                        continue;
                    }
                    if (isLineBeforeStatement(line) && statementIsAcceptableToCurrRdms()) {
                        processStatement(reader);
                        onlyIf = null;
                        skipIfSet.clear();
                    }
                }
            }
            numFilesVisited += 1;
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc)
        {
            System.out.println("Failed to access file: " + file.toString());
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc)
        {
            return CONTINUE;
        }
    }
}
