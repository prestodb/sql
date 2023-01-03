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
package com.facebook.coresql.linter.warning;

import com.facebook.coresql.parser.AstNode;
import org.junit.jupiter.api.Test;

import static com.facebook.coresql.parser.ParserHelper.parseStatement;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestWarningCollector
{
    private static final AstNode AST_NODE = parseStatement("SELECT true or false and false;");
    private static final WarningCollector COLLECTOR = new DefaultWarningCollector(new WarningCollectorConfig().setMaxWarnings(1));

    private static void assertCollectorHasOneWarning()
    {
        assertEquals(COLLECTOR.getAllWarnings().size(), 1);
    }

    @Test
    public void addWarningTest()
    {
        COLLECTOR.add(new WarningCode(0, "test code"), "msg", AST_NODE);
        assertCollectorHasOneWarning();
    }

    @Test
    public void testWarningsExceedMaxInConfig()
    {
        COLLECTOR.add(new WarningCode(0, "test code"), "msg", AST_NODE);
        COLLECTOR.add(new WarningCode(0, "test code"), "msg", AST_NODE);
        assertCollectorHasOneWarning();
    }
}
