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
package com.manticore.jsqlformatter;

import com.facebook.coresql.parser.AstNode;
import com.facebook.coresql.parser.ParserHelper;
import com.facebook.coresql.parser.Unparser;
import org.junit.jupiter.api.Assertions;

import java.util.regex.Pattern;

public final class TestUtils
{
    private static final Pattern SQL_COMMENT_PATTERN = Pattern.compile("(--.*$)|(/\\*.*?\\*/)", Pattern.MULTILINE);

    private static final Pattern SQL_SANITATION_PATTERN = Pattern.compile("(\\s+)", Pattern.MULTILINE);

    // Assure SPACE around Syntax Characters
    private static final Pattern SQL_SANITATION_PATTERN2 = Pattern.compile("\\s*([!/,()=+\\-*|\\]<>:])\\s*", Pattern.MULTILINE);

    private TestUtils()
    {}

    public static String buildSqlString(final String originalSql, boolean relaxed)
    {
        if (relaxed) {
            // remove comments
            String sanitizedSqlStr = SQL_COMMENT_PATTERN.matcher(originalSql).replaceAll("");

            // redundant white space
            sanitizedSqlStr = SQL_SANITATION_PATTERN.matcher(sanitizedSqlStr).replaceAll(" ");

            // assure spacing around Syntax Characters
            sanitizedSqlStr = SQL_SANITATION_PATTERN2.matcher(sanitizedSqlStr).replaceAll("$1");
            return sanitizedSqlStr.trim().toLowerCase();
        }
        else {
            // remove comments only
            return SQL_COMMENT_PATTERN.matcher(originalSql).replaceAll("");
        }
    }

    public static void assertParseAndUnparse(String sqlStr, boolean relaxed)
    {
        String expectedSqlStr = buildSqlString(sqlStr, relaxed);

        AstNode ast = ParserHelper.parseStatement(sqlStr);
        Assertions.assertNotNull(ast);
        String actualSqlStr = buildSqlString(Unparser.unparse(ast), relaxed);

        Assertions.assertEquals(expectedSqlStr, actualSqlStr);
    }

    public static void assertParseAndUnparse(String sqlStr)
    {
        assertParseAndUnparse(sqlStr, true);
    }
}
