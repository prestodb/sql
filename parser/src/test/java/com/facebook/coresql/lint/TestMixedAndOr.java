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
package com.facebook.coresql.lint;

import com.facebook.coresql.parser.AstNode;
import com.facebook.coresql.warning.DefaultWarningCollector;
import com.facebook.coresql.warning.WarningCollectorConfig;
import org.testng.annotations.Test;

import static com.facebook.coresql.parser.ParserHelper.parseStatement;
import static com.facebook.coresql.warning.WarningHandlingLevel.NORMAL;
import static org.testng.Assert.assertEquals;

public class TestMixedAndOr
{
    private static final LintingVisitor lintingVisitor = new MixedAndOr(new DefaultWarningCollector(new WarningCollectorConfig(), NORMAL));
    private static final String[] nonWarningSqlStrings = new String[] {
            "SELECT (true or false) and false;",
            "SELECT true or false or true;",
            "SELECT true and false and false;",
            "SELECT a FROM T WHERE a.id = 2 or (a.id = 3 and a.age = 73);",
            "SELECT a FROM T WHERE (a.id = 2 or a.id = 3) and (a.age = 73 or a.age = 100);",
            "SELECT * from Evaluation e JOIN Value v ON e.CaseNum = v.CaseNum\n" +
                    "    AND e.FileNum = v.FileNum AND e.ActivityNum = v.ActivityNum;",
            "use a.b;",
            " SELECT 1;",
            "SELECT a FROM T;",
            "SELECT a FROM T WHERE p1 > p2;",
            "SELECT a, b, c FROM T WHERE c1 < c2 and c3 < c4;",
            "SELECT CASE a WHEN IN ( 1 ) THEN b ELSE c END AS x, b, c FROM T WHERE c1 < c2 and c3 < c4;",
            "SELECT T.* FROM T JOIN W ON T.x = W.x;",
            "SELECT NULL;",
            "SELECT ARRAY[x] FROM T;",
            "SELECT TRANSFORM(ARRAY[x], x -> x + 2) AS arra FROM T;",
            "CREATE TABLE T AS SELECT TRANSFORM(ARRAY[x], x -> x + 2) AS arra FROM T;",
            "INSERT INTO T SELECT TRANSFORM(ARRAY[x], x -> x + 2) AS arra FROM T;",
            "SELECT ROW_NUMBER() OVER(PARTITION BY x) FROM T;",
            "SELECT x, SUM(y) OVER (PARTITION BY y ORDER BY 1) AS min\n" +
                    "FROM (values ('b',10), ('a', 10)) AS T(x, y)\n;",
            "SELECT\n" +
                    " CAST(MAP() AS map<bigint,array<boolean>>) AS \"bool_tensor_features\";",
            "SELECT f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f())))))))))))))))))))))))))))));",
            "SELECT abs, 2 as abs;",
    };

    private static final String[] warningSqlStrings = new String[] {
            "SELECT true or false and false;",
            "SELECT a FROM T WHERE a.id = 2 or a.id = 3 and a.age = 73;",
            "SELECT a FROM T WHERE (a.id = 2 or a.id = 3) and a.age = 73 or a.age = 100;"
    };

    private AstNode parse(String sql)
    {
        return parseStatement(sql);
    }

    @Test
    public void testDoesntThrowsMixedAndOrWarning()
    {
        lintingVisitor.getWarningCollector().clearWarnings();
        for (String sql : nonWarningSqlStrings) {
            AstNode shouldNotThrowWarning = parse(sql);
            lintingVisitor.lint(shouldNotThrowWarning);
            assertEquals(lintingVisitor.getWarningCollector().getWarnings().size(), 0);
        }
    }

    @Test
    public void testThrowsMixedAndOrWarning()
    {
        lintingVisitor.getWarningCollector().clearWarnings();
        for (String sql : warningSqlStrings) {
            AstNode shouldThrowWarning = parse(sql);
            lintingVisitor.lint(shouldThrowWarning);
            assertEquals(lintingVisitor.getWarningCollector().getWarnings().size(), 1);
        }
    }
}
