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

import com.facebook.coresql.parser.sqllogictest.java.SqlLogicTest.DatabaseDialect;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class SqlLogicTestResult
{
    private final List<SqlLogicTestStatement> erroneousStatements;
    private final DatabaseDialect databaseDialect;
    private int numFilesVisited;
    private int numStatementsFound;

    public SqlLogicTestResult(DatabaseDialect databaseDialect)
    {
        this.databaseDialect = requireNonNull(databaseDialect);
        this.erroneousStatements = new ArrayList<>();
    }

    public DatabaseDialect getDatabaseDialect()
    {
        return databaseDialect;
    }

    public void addErroneousStatements(List<SqlLogicTestStatement> erroneousStatements)
    {
        this.erroneousStatements.addAll(erroneousStatements);
    }

    public List<SqlLogicTestStatement> getErroneousStatements()
    {
        return ImmutableList.copyOf(erroneousStatements);
    }

    public int getNumFilesVisited()
    {
        return numFilesVisited;
    }

    public void incrementNumFilesVisited()
    {
        numFilesVisited += 1;
    }

    public int getNumStatementsFound()
    {
        return numStatementsFound;
    }

    public void incrementNumStatementsFound(int incrementAmount)
    {
        this.numStatementsFound += incrementAmount;
    }

    public int getNumStatementsParsed()
    {
        return numStatementsFound - erroneousStatements.size();
    }
}
