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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.facebook.coresql.parser.sqllogictest.java.SqlLogicTest.CoreSqlParsingError;
import static java.util.Objects.requireNonNull;

public class SqlLogicTestStatement
{
    private final Optional<DatabaseDialect> onlyIfDialect;
    private final Optional<CoreSqlParsingError> error;
    private final Set<DatabaseDialect> skipIfSet;
    private final String statement;

    private SqlLogicTestStatement(Builder builder)
    {
        this.onlyIfDialect = builder.onlyIfDialect;
        this.error = builder.error;
        this.skipIfSet = builder.skipIfSet;
        this.statement = builder.statement;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public Optional<DatabaseDialect> getCurrStatementsOnlyIfDialect()
    {
        return onlyIfDialect;
    }

    public Optional<CoreSqlParsingError> getError()
    {
        return error;
    }

    public Set<DatabaseDialect> getSkipIfSet()
    {
        return skipIfSet;
    }

    public String getStatement()
    {
        return statement;
    }

    public static class Builder
    {
        private Optional<DatabaseDialect> onlyIfDialect;
        private Optional<CoreSqlParsingError> error;
        private final Set<DatabaseDialect> skipIfSet;
        private String statement;

        public Builder()
        {
            this.onlyIfDialect = Optional.empty();
            this.error = Optional.empty();
            this.skipIfSet = new HashSet<>();
        }

        public SqlLogicTestStatement build()
        {
            return new SqlLogicTestStatement(this);
        }

        public void setOnlyIfDialect(DatabaseDialect onlyIfDialect)
        {
            this.onlyIfDialect = Optional.of(onlyIfDialect);
        }

        public void addDialectToSkipIfSet(DatabaseDialect dialect)
        {
            this.skipIfSet.add(requireNonNull(dialect));
        }

        public Builder setError(Optional<CoreSqlParsingError> error)
        {
            this.error = error;
            return this;
        }

        public Builder setStatement(String statement)
        {
            this.statement = requireNonNull(statement);
            return this;
        }
    }
}
