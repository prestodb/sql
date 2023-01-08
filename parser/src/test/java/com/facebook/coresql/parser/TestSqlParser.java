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

import com.facebook.coresql.parser.sqllogictest.java.SqlLogicTest;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.facebook.coresql.parser.ParserHelper.parseExpression;
import static com.facebook.coresql.parser.ParserHelper.parseStatement;
import static com.facebook.coresql.parser.SqlParserConstants.LESS_THAN;
import static com.facebook.coresql.parser.SqlParserConstants.NOT;
import static com.facebook.coresql.parser.SqlParserConstants.PLUS;
import static com.facebook.coresql.parser.SqlParserConstants.SQRT;
import static com.facebook.coresql.parser.SqlParserConstants.tokenImage;
import static com.facebook.coresql.parser.Unparser.unparse;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class TestSqlParser
{
    private static final String[] TEST_SQL_TESTSTRINGS = new String[] {
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
            "SELECT sqrt(x), power(y, 5), myFunction('a') FROM T;",
            "SELECT 1 ఒకటి;",
            "SELECT a_b(a,'a', 1);",
            "SELECT if(regexp_like(content_fbtype,'comment'),content_id,container_post_fbid) as content_id;",
            "SELECT if(is_test_user(content_fbtype,'comment'),content_id,container_post_fbid) as content_id;",
    };

    private AstNode parse(String sql)
    {
        return parseStatement(sql);
    }

    @Test
    public void smokeTest()
    {
        for (String sql : TEST_SQL_TESTSTRINGS) {
            assertNotNull(parse(sql));
        }
    }

    @Test
    public void parseUnparseTest()
    {
        for (String sql : TEST_SQL_TESTSTRINGS) {
            System.out.println(sql);
            AstNode ast = parse(sql);
            assertNotNull(ast);
            assertEquals(sql.trim(), unparse(ast).trim());
        }
    }

    @Test
    public void sqlLogicTest()
            throws IOException
    {
        SqlLogicTest.execute();
    }

    @Test
    public void testGetOperator()
    {
        assertEquals(parseExpression("x + 10").GetOperator(), PLUS);
        assertEquals(parseExpression("x < /*comment*/ 10").GetOperator(), LESS_THAN);
        assertEquals(parseExpression("NOT x").GetOperator(), NOT);
    }

    @Test
    public void testGetFunctionName()
    {
        assertEquals(parseExpression("SQRT(10)").GetFunctionName(), tokenImage[SQRT].substring(1, tokenImage[SQRT].length() - 1));
        assertEquals(parseExpression("POW(x, 2)").GetFunctionName(), "POW");
        assertEquals(parseExpression("PoW(x, 2)").GetFunctionName(), "PoW");
        assertEquals(parseExpression("MyFunction('a')").GetFunctionName(), "MyFunction");
    }

    @Test
    public void testIsNegated()
    {
        assertEquals(parseExpression("a LIKE B").IsNegated(), false);
        assertEquals(parseExpression("a NOT LIKE B").IsNegated(), true);
        assertEquals(parseExpression("a IS NULL").IsNegated(), false);
        assertEquals(parseExpression("a IS NOT NULL").IsNegated(), true);
    }
}
