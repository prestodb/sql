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
        // String sqlStr = "SELECT :test;";
        // String sqlStr = "SELECT NEXT VALUE FOR a from b;";
        AstNode astNode = TestUtils.assertParseAndUnparse(sqlStr);
        System.out.println(Unparser.unparse(astNode));
        try {
            System.out.println(TestUtils.formatToTree(astNode));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
