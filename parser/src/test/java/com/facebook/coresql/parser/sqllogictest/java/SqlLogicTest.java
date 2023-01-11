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

package com.facebook.coresql.parser.sqllogictest.java;

import com.facebook.coresql.parser.AstNode;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.airlift.bootstrap.Bootstrap;
import io.airlift.log.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.facebook.coresql.parser.ParserHelper.parseStatement;
import static com.facebook.coresql.parser.Unparser.unparse;
import static com.facebook.coresql.parser.sqllogictest.java.SqlLogicTest.CoreSqlParsingError.PARSING_ERROR;
import static com.facebook.coresql.parser.sqllogictest.java.SqlLogicTest.CoreSqlParsingError.UNPARSED_DOES_NOT_MATCH_ORIGINAL_ERROR;
import static com.facebook.coresql.parser.sqllogictest.java.SqlLogicTest.CoreSqlParsingError.UNPARSING_ERROR;
import static com.facebook.coresql.parser.sqllogictest.java.SqlLogicTest.DatabaseDialect.ALL;
import static java.lang.Math.min;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.isHidden;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Files.walkFileTree;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public final class SqlLogicTest
{
    private static final String NAME_OF_RESULT_FILE = "sqllogictest_result.txt";
    private static final Logger LOG = Logger.get(SqlLogicTest.class);

    private final SqlLogicTestResult result;
    private final FileVisitor<Path> fileVisitor;
    private final Path testFilesRootPath;
    private final Path resultPath;
    private final DatabaseDialect databaseDialect;
    private final int maxNumFilesToVisit;

    enum CoreSqlParsingError
    {
        PARSING_ERROR,
        UNPARSING_ERROR,
        UNPARSED_DOES_NOT_MATCH_ORIGINAL_ERROR
    }

    enum DatabaseDialect
    {
        ALL,
        ORACLE,
        MYSQL,
        MSSQL,
        SQLITE,
        POSTGRESQL
    }

    @Inject
    public SqlLogicTest(SqlLogicTestConfig config)
    {
        this.testFilesRootPath = requireNonNull(config.getTestFilesRootPath(), "test files root path is null");
        this.resultPath = requireNonNull(config.getResultPath(), "result path is null");
        this.databaseDialect = requireNonNull(config.getDatabaseDialect(), "database dialect is null");
        this.result = new SqlLogicTestResult(config.getDatabaseDialect());
        this.fileVisitor = new SqlLogicTestFileVisitor();
        this.maxNumFilesToVisit = config.getMaxNumFilesToVisit();
    }

    public static void execute()
            throws IOException
    {
        Bootstrap app = new Bootstrap(new SqlLogicTestModule());

        Injector injector = app
                .doNotInitializeLogging()
                .quiet()
                .initialize();

        injector.getInstance(SqlLogicTest.class).run();
    }

    private void run()
            throws IOException
    {
        createFileForResult(resultPath);
        walkFileTree(testFilesRootPath, fileVisitor);
        writeTestResultToFile(result, resultPath);
    }

    private static void createFileForResult(Path resultPath)
            throws IOException
    {
        try {
            createFile(resultPath.resolve(NAME_OF_RESULT_FILE));
        }
        catch (FileAlreadyExistsException e) {
            LOG.info("Result file already exists. We'll overwrite it with the new result.");
        }
        catch (IOException e) {
            throw new IOException("Could not create the sqllogictest result file.", e);
        }
    }

    private Optional<CoreSqlParsingError> checkStatementForParserError(String statement)
    {
        Optional<AstNode> ast = Optional.ofNullable(parseStatement(statement));
        if (!ast.isPresent()) {
            return Optional.of(PARSING_ERROR);
        }
        String unparsed;
        try {
            unparsed = unparse(ast.get());
        }
        catch (Exception e) {
            return Optional.of(UNPARSING_ERROR);
        }
        if (!statement.trim().equals(unparsed.trim())) {
            return Optional.of(UNPARSED_DOES_NOT_MATCH_ORIGINAL_ERROR);
        }
        return Optional.empty();
    }

    private static void writeTestResultToFile(SqlLogicTestResult result, Path resultPath)
    {
        try (PrintWriter writer = new PrintWriter(resultPath.resolve(NAME_OF_RESULT_FILE).toString())) {
            for (SqlLogicTestStatement statement : result.getErroneousStatements()) {
                writer.print(statement.getStatement() + " ----> " + statement.getError().get().name());
                writer.println();
                writer.println();
            }
            writer.println("===============================================");
            writer.println("RESULT");
            writer.println("===============================================");
            writer.printf("Database dialect tested against: %s", result.getDatabaseDialect());
            writer.println();
            writer.printf("%d statements found, %d statements successfully parsed.", result.getNumStatementsFound(), result.getNumStatementsParsed());
            writer.println();
            writer.printf("%s%% of sqllogictest statements were successfully parsed.",
                    new DecimalFormat("#.##").format(result.getNumStatementsParsed() / (double) result.getNumStatementsFound() * 100));
        }
        catch (FileNotFoundException e) {
            LOG.error(e, "Could not find the result file at the given location");
        }
    }

    private boolean isLineBeforeStatement(String line)
    {
        return line.startsWith("statement ok") || line.startsWith("query");
    }

    private boolean statementIsAcceptableToCurrDialect(SqlLogicTestStatement sqlLogicTestStatement)
    {
        if (databaseDialect == ALL) {
            return sqlLogicTestStatement.getSkipIfSet().isEmpty() && !sqlLogicTestStatement.getCurrStatementsOnlyIfDialect().isPresent();
        }
        if (sqlLogicTestStatement.getCurrStatementsOnlyIfDialect().isPresent() && sqlLogicTestStatement.getCurrStatementsOnlyIfDialect().get() != databaseDialect) {
            return false;
        }
        return !sqlLogicTestStatement.getSkipIfSet().contains(databaseDialect);
    }

    private boolean isConditionalRecordLine(String line)
    {
        String[] splitLine = line.split(" ");
        if (splitLine.length <= 1) {
            return false;
        }
        String conditionalFlag = splitLine[0];
        if (!conditionalFlag.equals("skipif") && !conditionalFlag.equals("onlyif")) {
            return false;
        }
        String potentialDialect = splitLine[1];
        try {
            DatabaseDialect.valueOf(potentialDialect.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            LOG.debug("This line had a skip-if or only-if dialect that isn't supported: %s", line);
            return false;
        }
        return true;
    }

    private void processConditionalRecordLine(String line, SqlLogicTestStatement.Builder builder)
    {
        String[] splitLine = line.split(" ");
        if (splitLine.length <= 1) {
            return;
        }
        String potentialDialect = splitLine[1];
        DatabaseDialect dialect = DatabaseDialect.valueOf(potentialDialect.toUpperCase());
        if (line.startsWith("onlyif")) {
            builder.setOnlyIfDialect(dialect);
        }
        else if (line.startsWith("skipif")) {
            builder.addDialectToSkipIfSet(dialect);
        }
    }

    private static String getCleanedUpStatement(StringBuilder statement)
    {
        statement = new StringBuilder(statement.toString().trim());
        if (statement.length() > 0 && !statement.toString().endsWith(";")) {
            statement.append(";"); // This semi-colon is necessary for parsing.
        }
        return statement.toString();
    }

    private int getStartingIndexOfNextStatement(int currIdx, List<String> statements)
    {
        while (currIdx < statements.size() && !statements.get(currIdx).equals("")) {
            ++currIdx;
        }
        return currIdx;
    }

    private int getIndexOfNextDelimiter(int currIdx, List<String> statements)
    {
        while (currIdx < statements.size() && !statements.get(currIdx).equals("----")) {
            ++currIdx;
        }
        return currIdx;
    }

    private List<List<String>> getListOfStatementsAsLines(List<String> linesFromFile)
    {
        List<List<String>> statementsAsListsOfLines = new ArrayList<>();
        int currIdx = 0;
        while (currIdx < linesFromFile.size()) {
            if (isConditionalRecordLine(linesFromFile.get(currIdx)) || isLineBeforeStatement(linesFromFile.get(currIdx))) {
                int startingIdxOfNextStatement = getStartingIndexOfNextStatement(currIdx, linesFromFile);
                int endOfCurrStatement = min(startingIdxOfNextStatement, getIndexOfNextDelimiter(currIdx, linesFromFile));
                statementsAsListsOfLines.add(linesFromFile.subList(currIdx, endOfCurrStatement));
                currIdx = startingIdxOfNextStatement;
            }
            else {
                ++currIdx;
            }
        }
        return statementsAsListsOfLines;
    }

    private SqlLogicTestStatement buildSqlLogicTestStatementFromLines(List<String> statementAsListsOfLines)
    {
        SqlLogicTestStatement.Builder builder = SqlLogicTestStatement.builder();
        StringBuilder statement = new StringBuilder();
        for (String line : statementAsListsOfLines) {
            if (isConditionalRecordLine(line)) {
                processConditionalRecordLine(line, builder);
            }
            else if (!isLineBeforeStatement(line)) {
                statement.append(line);
            }
        }
        String cleanStatement = getCleanedUpStatement(statement);
        return builder.setStatement(cleanStatement).setError(checkStatementForParserError(cleanStatement)).build();
    }

    private List<SqlLogicTestStatement> parseSqlLogicTestStatementsFromFile(Path file)
            throws IOException
    {
        List<String> lines = readAllLines(file);
        List<List<String>> statementsAsListsOfLines = getListOfStatementsAsLines(lines);
        return statementsAsListsOfLines.stream()
                .map(this::buildSqlLogicTestStatementFromLines)
                .filter(this::statementIsAcceptableToCurrDialect)
                .collect(toList());
    }

    private List<SqlLogicTestStatement> getErroneousStatements(List<SqlLogicTestStatement> testStatements)
    {
        return testStatements.stream()
                .filter(statement -> checkStatementForParserError(statement.getStatement()).isPresent())
                .collect(toList());
    }

    private class SqlLogicTestFileVisitor
            implements FileVisitor<Path>
    {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
        {
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException
        {
            if (isHidden(file)) {
                return CONTINUE;
            }
            if (result.getNumFilesVisited() >= maxNumFilesToVisit) {
                return TERMINATE;
            }
            result.incrementNumFilesVisited();
            List<SqlLogicTestStatement> testStatements = parseSqlLogicTestStatementsFromFile(file);
            result.incrementNumStatementsFound(testStatements.size());
            List<SqlLogicTestStatement> erroneousStatements = getErroneousStatements(testStatements);
            result.addErroneousStatements(erroneousStatements);
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exception)
        {
            LOG.warn(exception, "Failed to access file: %s", file.toString());
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exception)
        {
            return CONTINUE;
        }
    }
}
