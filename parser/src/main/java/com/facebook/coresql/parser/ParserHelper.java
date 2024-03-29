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
package com.facebook.coresql.parser;

import java.io.StringReader;

public class ParserHelper
{
    private ParserHelper() {}

    public static AstNode parseStatement(String sql)
    {
        SqlParser parser = new SqlParser(new SqlParserTokenManager(new SimpleCharStream(new StringReader(sql), 1, 1)));
        try {
            parser.direct_SQL_statement();
            return parser.getResult();
        }
        catch (ParseException pe) {
            return null;
        }
    }

    public static AstNode parseExpression(String expression)
    {
        SqlParser parser = new SqlParser(new SqlParserTokenManager(new SimpleCharStream(new StringReader(expression), 1, 1)));
        try {
            parser.derived_column();
            return parser.getResult();
        }
        catch (ParseException pe) {
            return null;
        }
    }
}
