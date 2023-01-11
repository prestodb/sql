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
import io.airlift.configuration.Config;
import io.airlift.configuration.ConfigDescription;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.facebook.coresql.parser.sqllogictest.java.SqlLogicTest.DatabaseDialect.ALL;

public class SqlLogicTestConfig
{
    private Path testFilesRootPath = Paths.get("src/test/java/com/facebook/coresql/parser/sqllogictest/resources/tests");
    private Path resultPath = Paths.get("src/test/java/com/facebook/coresql/parser/sqllogictest/resources");
    private DatabaseDialect databaseDialect = ALL;
    private int maxNumFilesToVisit = 10;

    @Config("test-files-root-path")
    public SqlLogicTestConfig setTestFilesRootPath(String testFilesRootPath)
    {
        this.testFilesRootPath = Paths.get(testFilesRootPath);
        return this;
    }

    @NotNull
    public Path getTestFilesRootPath()
    {
        return testFilesRootPath;
    }

    @Config("result-path")
    public SqlLogicTestConfig setResultPath(String resultPath)
    {
        this.resultPath = Paths.get(resultPath);
        return this;
    }

    @NotNull
    public Path getResultPath()
    {
        return resultPath;
    }

    @Config("max-num-files-to-visit")
    @ConfigDescription("The max number of test files to test against")
    public SqlLogicTestConfig setMaxNumFilesToVisit(int maxNumFilesToVisit)
    {
        this.maxNumFilesToVisit = maxNumFilesToVisit;
        return this;
    }

    @Min(1)
    public int getMaxNumFilesToVisit()
    {
        return maxNumFilesToVisit;
    }

    @Config("database-dialect")
    @ConfigDescription("The database dialect to filter test queries over")
    public SqlLogicTestConfig setDatabaseDialect(DatabaseDialect databaseDialect)
    {
        this.databaseDialect = databaseDialect;
        return this;
    }

    @NotNull
    public DatabaseDialect getDatabaseDialect()
    {
        return databaseDialect;
    }
}
