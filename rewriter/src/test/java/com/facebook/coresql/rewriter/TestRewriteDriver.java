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
import com.google.common.collect.ImmutableSet;
import org.testng.annotations.Test;

import java.util.Optional;

import static com.facebook.coresql.parser.ParserHelper.parseStatement;
import static com.facebook.coresql.parser.Unparser.unparse;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class TestRewriteDriver
{
    private static final String[] STATEMENT_THAT_DOESNT_NEED_REWRITE = new String[] {
            "CREATE TABLE blah AS SELECT * FROM (SELECT * FROM (SELECT foo FROM T ORDER BY x LIMIT 10) ORDER BY y LIMIT 10) ORDER BY z LIMIT 10;",
            "SELECT dealer_id, sales OVER (PARTITION BY dealer_id ORDER BY sales);",
            "INSERT INTO blah SELECT * FROM (SELECT t.date, t.code, t.qty FROM sales AS t ORDER BY t.date LIMIT 100);",
            "SELECT (true or false) and false;",
            "SELECT * FROM T ORDER BY y;",
            "SELECT * FROM T ORDER BY y LIMIT 10;",
            "use a.b;",
            "SELECT 1;",
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
            "SELECT abs, 2 as abs;"
    };

    private static final String[] STATEMENT_BEFORE_REWRITE = new String[] {
            // ORDER BY Anti-Pattern
            "CREATE TABLE blah AS SELECT * FROM T ORDER BY y;",
            "INSERT INTO blah SELECT * FROM (SELECT t.date, t.code, t.qty FROM sales AS t ORDER BY t.date) LIMIT 10;",
            "CREATE TABLE blah AS SELECT * FROM (SELECT * FROM (SELECT foo FROM T ORDER BY x LIMIT 10) ORDER BY y) ORDER BY z LIMIT 10;",
    };

    private static final String[] STATEMENT_AFTER_REWRITE = new String[] {
            // ORDER BY Anti-Pattern
            "CREATE TABLE blah AS SELECT * FROM T;",
            "INSERT INTO blah SELECT * FROM (SELECT t.date, t.code, t.qty FROM sales AS t) LIMIT 10;",
            "CREATE TABLE blah AS SELECT * FROM (SELECT * FROM (SELECT foo FROM T ORDER BY x LIMIT 10)) ORDER BY z LIMIT 10;",
    };

    private static final RewriteDriverConfig USE_ALL_REWRITERS_CONFIG = new RewriteDriverConfig().setUserEnabledRewriters(ImmutableSet.of(OrderByRewriter.class));
    private static final int EXPECTED_SIZE_OF_REWRITE_RESULT_LIST = 1;

    private void assertStatementUnchanged(String originalStatement)
    {
        Optional<RewriteDriverResult> result = getRewriteDriverResult(originalStatement, USE_ALL_REWRITERS_CONFIG);
        assertFalse(result.isPresent());
    }

    private void assertStatementRewritten(String originalStatement, String expectedStatement)
    {
        Optional<RewriteDriverResult> result = getRewriteDriverResult(originalStatement, USE_ALL_REWRITERS_CONFIG);
        assertTrue(result.isPresent());
        assertEquals(result.get().getRewriteResults().size(), EXPECTED_SIZE_OF_REWRITE_RESULT_LIST);
        AstNode rewrittenAst = result.get().getRewrittenSqlAsAst();
        String actualStatement = unparse(rewrittenAst).trim();
        assertEquals(actualStatement, expectedStatement);
    }

    private Optional<RewriteDriverResult> getRewriteDriverResult(String originalStatement, RewriteDriverConfig config)
    {
        AstNode ast = parseStatement(originalStatement);
        assertNotNull(ast);
        return new RewriteDriver(config, ast).applyRewriters();
    }

    @Test
    public void applyAllRewritersTest()
    {
        for (int i = 0; i < STATEMENT_BEFORE_REWRITE.length; i++) {
            assertStatementRewritten(STATEMENT_BEFORE_REWRITE[i], STATEMENT_AFTER_REWRITE[i]);
        }

        for (String sql : STATEMENT_THAT_DOESNT_NEED_REWRITE) {
            assertStatementUnchanged(sql);
        }
    }

    @Test
    public void applyUnknownRewriterTest()
    {
        Rewriter unknownRewriter = new Rewriter()
        {
            @Override
            public Optional<RewriteResult> rewrite()
            {
                return Optional.empty();
            }
        };
        RewriteDriverConfig invalidConfig = new RewriteDriverConfig().setUserEnabledRewriters(ImmutableSet.of(unknownRewriter.getClass()));
        Optional<RewriteDriverResult> rewriteResult = getRewriteDriverResult(STATEMENT_BEFORE_REWRITE[0], invalidConfig);
        assertFalse(rewriteResult.isPresent());
    }
}
