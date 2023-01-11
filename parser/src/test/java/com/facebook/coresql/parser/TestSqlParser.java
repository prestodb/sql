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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.facebook.coresql.parser.ParserHelper.parseExpression;
import static com.facebook.coresql.parser.ParserHelper.parseStatement;
import static com.facebook.coresql.parser.SqlParserConstants.LESS_THAN;
import static com.facebook.coresql.parser.SqlParserConstants.NOT;
import static com.facebook.coresql.parser.SqlParserConstants.PLUS;
import static com.facebook.coresql.parser.SqlParserConstants.SQRT;
import static com.facebook.coresql.parser.SqlParserConstants.tokenImage;
import static com.facebook.coresql.parser.Unparser.unparse;

public class TestSqlParser
{
    public static Stream<String> sqlStrings()
    {
        List<String> sqlStrings = Arrays.asList(
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
                "SELECT x, SUM(y) OVER (PARTITION BY y ORDER BY 1) AS min\n" + "FROM (values ('b',10), ('a', 10)) AS T(x, y)\n;",
                "SELECT\n" + " CAST(MAP() AS map<bigint,array<boolean>>) AS \"bool_tensor_features\";",
                "SELECT f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f(f())))))))))))))))))))))))))))));",
                "SELECT abs, 2 as abs;",
                "SELECT sqrt(x), power(y, 5), myFunction('a') FROM T;",
                "SELECT concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat('1','2'),'3'),'4'),'5'),'6'),'7'),'8'),'9'),'10'),'11'),'12'),'13'),'14'),'15'),'16'),'17'),'18'),'19'),'20'),'21'),col1 FROM tbl t1;");
        return sqlStrings.stream();
    }

    private AstNode parse(String sql)
    {
        return parseStatement(sql);
    }

    @ParameterizedTest(name = "SQL {0}")
    @MethodSource("sqlStrings")
    public void smokeTest(String sqlStr)
    {
        Assertions.assertNotNull(parse(sqlStr), "Failed SQL:\n" + sqlStr);
    }

    @ParameterizedTest(name = "SQL {0}")
    @MethodSource("sqlStrings")
    public void parseUnparseTest(String sqlStr)
    {
        AstNode ast = parse(sqlStr);
        Assertions.assertNotNull(ast);
        Assertions.assertEquals(sqlStr.trim(), unparse(ast).trim(), "Failed SQL:\n" + sqlStr);
    }

    @Test
    public void sqlLogicTest() throws IOException
    {
        SqlLogicTest.execute();
    }

    @Test
    public void testGetOperator()
    {
        Assertions.assertEquals(PLUS, parseExpression("x + 10").GetOperator());
        Assertions.assertEquals(LESS_THAN, parseExpression("x < /*comment*/ 10").GetOperator());
        Assertions.assertEquals(NOT, parseExpression("NOT x").GetOperator());
    }

    @Test
    public void testGetFunctionName()
    {
        Assertions.assertEquals(
                parseExpression("SQRT(10)").GetFunctionName(),
                tokenImage[SQRT].substring(1, tokenImage[SQRT].length() - 1));
        Assertions.assertEquals("POW", parseExpression("POW(x, 2)").GetFunctionName());
        Assertions.assertEquals("PoW", parseExpression("PoW(x, 2)").GetFunctionName());
        Assertions.assertEquals("MyFunction", parseExpression("MyFunction('a')").GetFunctionName());
    }

    @Test
    public void testIsNegated()
    {
        Assertions.assertFalse(parseExpression("a LIKE B").IsNegated());
        Assertions.assertTrue(parseExpression("a NOT LIKE B").IsNegated());
        Assertions.assertFalse(parseExpression("a IS NULL").IsNegated());
        Assertions.assertTrue(parseExpression("a IS NOT NULL").IsNegated());
    }
}
