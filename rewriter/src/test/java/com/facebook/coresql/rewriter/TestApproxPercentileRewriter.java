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

public class TestApproxPercentileRewriter
{
    private static final String[] STATEMENT_THAT_DOESNT_NEED_REWRITE = new String[] {
            // True Negative
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
            "SELECT APPROX_PERCENTILE(x, 0.1) AS percentile_10, APPROX_PERCENTILE(y, 0.2) AS percentile_20, APPROX_PERCENTILE(z, 0.3) AS percentile_30 FROM T;",
            "SELECT APPROX_PERCENTILE(x, ARRAY[0.1, 0.2, 0.3]), APPROX_PERCENTILE(x, 0.4) FROM T;",
            "SELECT APPROX_PERCENTILE(x, ARRAY[0.1, 0.2, 0.3]) FROM (SELECT APPROX_PERCENTILE(x, 0.1) from T);"
    };

    private static final String[] STATEMENT_BEFORE_REWRITE = new String[] {
            "SELECT APPROX_PERCENTILE(x, 0.1) AS percentile_10, APPROX_PERCENTILE(x, 0.2) AS percentile_20, APPROX_PERCENTILE(x, 0.3) AS percentile_30 FROM T;",
            "SELECT APPROX_PERCENTILE(x, 0.1), APPROX_PERCENTILE(x, 0.2) AS percentile_20, APPROX_PERCENTILE(x, 0.3) FROM T;",
            "SELECT approx_percentile(y, 0.2),  x + 1, approx_percentile(y, 0.1) from T group by 2;",
            "SELECT APPROX_PERCENTILE(x, 0.1) AS percentile_10, APPROX_PERCENTILE(x, 0.2) AS percentile_20, APPROX_PERCENTILE(z, 0.3) AS percentile_30 FROM T;",
            "SELECT APPROX_PERCENTILE(x, ARRAY[0.1, 0.2, 0.3]), APPROX_PERCENTILE(x, 0.4), APPROX_PERCENTILE(x, 0.5) FROM T;",
            "SELECT APPROX_PERCENTILE(x, 0.1) FROM (SELECT APPROX_PERCENTILE(x, 0.2) FROM T);",
            "SELECT x FROM (SELECT APPROX_PERCENTILE(x, 0.1) AS percentile_10, APPROX_PERCENTILE(x, 0.2) AS percentile_20, APPROX_PERCENTILE(x, 0.3) AS percentile_30 FROM T);",
            "CREATE TABLE blah AS SELECT * FROM (SELECT * FROM (SELECT approx_percentile(y, 0.2),  x + 1, approx_percentile(y, 0.1) from T group by 2));",
            "SELECT APPROX_PERCENTILE(x, 0.1) FROM (SELECT * FROM (SELECT approx_percentile(x, 0.1), approx_percentile(y, 0.1) from T group by 2));"
    };

    private static final String[] STATEMENT_AFTER_REWRITE = new String[] {
            "SELECT APPROX_PERCENTILE(x, ARRAY[0.1, 0.2, 0.3])[1] AS percentile_10, APPROX_PERCENTILE(x, ARRAY[0.1, 0.2, 0.3])[2] AS percentile_20, APPROX_PERCENTILE(x, ARRAY[0.1, 0.2, 0.3])[3] AS percentile_30 FROM T;",
            "SELECT APPROX_PERCENTILE(x, ARRAY[0.1, 0.2, 0.3])[1], APPROX_PERCENTILE(x, ARRAY[0.1, 0.2, 0.3])[2] AS percentile_20, APPROX_PERCENTILE(x, ARRAY[0.1, 0.2, 0.3])[3] FROM T;",
            "SELECT approx_percentile(y, ARRAY[0.2, 0.1])[1],  x + 1, approx_percentile(y, ARRAY[0.2, 0.1])[2] from T group by 2;",
            "SELECT APPROX_PERCENTILE(x, ARRAY[0.1, 0.2])[1] AS percentile_10, APPROX_PERCENTILE(x, ARRAY[0.1, 0.2])[2] AS percentile_20, APPROX_PERCENTILE(z, 0.3) AS percentile_30 FROM T;",
            "SELECT APPROX_PERCENTILE(x, ARRAY[0.1, 0.2, 0.3]), APPROX_PERCENTILE(x, ARRAY[0.4, 0.5])[1], APPROX_PERCENTILE(x, ARRAY[0.4, 0.5])[2] FROM T;",
            "SELECT APPROX_PERCENTILE(x, ARRAY[0.1, 0.2])[1] FROM (SELECT APPROX_PERCENTILE(x, ARRAY[0.1, 0.2])[2] FROM T);",
            "SELECT x FROM (SELECT APPROX_PERCENTILE(x, ARRAY[0.1, 0.2, 0.3])[1] AS percentile_10, APPROX_PERCENTILE(x, ARRAY[0.1, 0.2, 0.3])[2] AS percentile_20, APPROX_PERCENTILE(x, ARRAY[0.1, 0.2, 0.3])[3] AS percentile_30 FROM T);",
            "CREATE TABLE blah AS SELECT * FROM (SELECT * FROM (SELECT approx_percentile(y, ARRAY[0.2, 0.1])[1],  x + 1, approx_percentile(y, ARRAY[0.2, 0.1])[2] from T group by 2));",
            "SELECT APPROX_PERCENTILE(x, ARRAY[0.1, 0.1])[1] FROM (SELECT * FROM (SELECT approx_percentile(x, ARRAY[0.1, 0.1])[2], approx_percentile(y, 0.1) from T group by 2));"
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
        return new ApproxPercentileRewriter(ast).rewrite();
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
