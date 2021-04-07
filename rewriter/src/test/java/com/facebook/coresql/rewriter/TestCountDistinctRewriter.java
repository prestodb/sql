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

package com.facebook.coresql.rewriter;

import com.facebook.coresql.parser.AstNode;
import org.testng.annotations.Test;

import java.util.Optional;

import static com.facebook.coresql.parser.ParserHelper.parseStatement;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class TestCountDistinctRewriter
{
    private static final String[] STATEMENT_THAT_DOESNT_NEED_REWRITE = new String[] {
            "CREATE TABLE blah AS SELECT * FROM (SELECT * FROM (SELECT foo FROM T ORDER BY x LIMIT 10) ORDER BY y LIMIT 10) ORDER BY z LIMIT 10;",
            "SELECT dealer_id, sales OVER (PARTITION BY dealer_id ORDER BY sales);",
            "INSERT INTO blah SELECT * FROM (SELECT t.date, t.code, t.qty FROM sales AS t ORDER BY t.date LIMIT 100);",
            "SELECT (true or false) and false;",
            "SELECT * FROM T ORDER BY y;",
            "SELECT * FROM T ORDER BY y LIMIT 10;",
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
            // False Positive
            "SELECT COUNT(DISTINCT x) > 1.5 FROM T;",
            "SELECT COUNT(x) > 1 FROM T;"
    };

    private static final String[] STATEMENT_BEFORE_REWRITE = new String[] {
            // True Positive
            "SELECT COUNT(DISTINCT x) > 1 AS has_dupes FROM T;",
            // Subquery
            "SELECT * FROM (SELECT COUNT(DISTINCT x) > 1 AS has_dupes FROM T);"
    };

    private static final String[] STATEMENT_AFTER_REWRITE = new String[] {
            // False Positive
            "SELECT MIN(x) IS DISTINCT FROM MAX(x)  AS has_dupes FROM T;",
            "SELECT * FROM (SELECT MIN(x) IS DISTINCT FROM MAX(x)  AS has_dupes FROM T);"
    };

    private void assertStatementUnchanged(String originalStatement)
    {
        assertFalse(getRewriteResult(originalStatement).isPresent());
    }

    private void assertStatementRewritten(String originalStatement, String expectedStatement)
    {
        Optional<RewriteResult> result = getRewriteResult(originalStatement);
        assertTrue(result.isPresent());
        assertEquals(result.get().getRewrittenSql(), expectedStatement);
    }

    private Optional<RewriteResult> getRewriteResult(String originalStatement)
    {
        AstNode ast = parseStatement(originalStatement);
        assertNotNull(ast);
        return new CountDistinctRewriter(ast).rewrite();
    }

    @Test
    public void rewriteTest()
    {
        for (int i = 0; i < STATEMENT_BEFORE_REWRITE.length; i++) {
            assertStatementRewritten(STATEMENT_BEFORE_REWRITE[i], STATEMENT_AFTER_REWRITE[i]);
        }

        for (String sql : STATEMENT_THAT_DOESNT_NEED_REWRITE) {
            assertStatementUnchanged(sql);
        }
    }
}
