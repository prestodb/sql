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
package com.facebook.coresql.parser.expression;

import com.facebook.coresql.parser.AstNode;
import com.facebook.coresql.parser.ParseException;
import com.facebook.coresql.parser.TestUtils;
import com.facebook.coresql.parser.Unparser;
import org.junit.jupiter.api.Test;

public class ParameterTest
{
    @Test
    public void testParameter() throws ParseException
    {
        // String sqlStr = "SELECT Overlaps( overlaps ) AS overlaps\n" + "FROM overlaps.overlaps overlaps\n" + "WHERE
        // overlaps = 'overlaps'\n" + " AND (CURRENT_TIME, INTERVAL '1' HOUR) OVERLAPS (CURRENT_TIME, INTERVAL -'1'
        // HOUR)\n" + ";";
        String sqlStr = "SELECT /*+parallel*/ sqrt(40);";
        AstNode astNode = TestUtils.assertParseAndUnparse(sqlStr);
        System.out.println(Unparser.unparse(astNode));
        try {
            System.out.println(TestUtils.formatToTree(astNode));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testComplexExpression() throws ParseException {
        String sqlStr = "SELECT ((3.0 >= 4.0 AND 5.0 <= 6.0) OR \n"
                        + "(7.0 < 8.0 AND 9.0 > 10.0) OR \n"
                        + "(11.0 = 11.0 AND 19.0 > 20.0) OR \n"
                        + "(17.0 = 14.0 AND 19.0 > 17.0) OR \n"
                        + "(17.0 = 18.0 AND 20.0 > 20.0) OR \n"
                        + "(17.0 = 16.0 AND 19.0 > 20.0) OR \n"
                        + "(17.0 = 18.0 AND 19.0 > 20.0) OR \n"
                        + "(17.0 = 18.0 AND 19.0 > 20.0) OR \n"
                        + "(17.0 = 22.0 AND 19.0 > 20.0) OR \n"
                        + "(18.0 = 18.0 AND 22.0 > 20.0) OR \n"
                        + "(17.0 = 18.0 AND 19.0 > 20.0) OR \n"
                        + "(18.0 = 18.0 AND 22.0 > 20.0) OR \n"
                        + "(18.0 = 19.0 AND 22.0 > 20.0) OR \n"
                        + "(117.0 = 22.0 AND 19.0 > 20.0) OR \n"
                        + "(118.0 = 18.0 AND 22.0 > 20.0) OR \n"
                        + "(117.0 = 18.0 AND 19.0 > 20.0) OR \n"
                        + "(17.0 = 18.0 AND 19.0 > 20.0));";
        AstNode astNode = TestUtils.assertParseAndUnparse(sqlStr);
    }

    @Test
    void testComplexExpression1() throws ParseException {
        String sqlStr = "SELECT ((3.0 >= 4.0 AND 5.0 <= 6.0) OR "
                        + "(7.0 < 8.0 AND 9.0 > 10.0) OR "
                        + "(11.0 = 11.0 AND 19.0 > 20.0) OR "
                        + "(17.0 = 14.0 AND 19.0 > 17.0) OR "
                        + "(17.0 = 18.0 AND 20.0 > 20.0) OR "
                        + "(17.0 = 16.0 AND 19.0 > 20.0));";
        AstNode astNode = TestUtils.assertParseAndUnparse(sqlStr);
    }

    @Test
    void testJoin() throws Exception {
        String sqlStr = "select * from a,b where a.a=b.b;";
        AstNode astNode = TestUtils.assertParseAndUnparse(sqlStr);
        System.out.println(TestUtils.formatToTree(astNode));
    }

    @Test
    void testUnicode() throws Exception {
        String sqlStr = "select * from U&\\मकान\\;";
        AstNode astNode = TestUtils.assertParseAndUnparse(sqlStr);
        System.out.println(TestUtils.formatToTree(astNode));
    }
}
