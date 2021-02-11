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

package com.facebook.coresql.linter.lint;

import com.facebook.coresql.linter.warning.WarningCode;
import com.facebook.coresql.linter.warning.WarningCollector;
import com.facebook.coresql.parser.AstNode;
import com.facebook.coresql.parser.SqlParserDefaultVisitor;

public abstract class LintingVisitor
        extends SqlParserDefaultVisitor
{
    private final WarningCollector warningCollector;

    public LintingVisitor(WarningCollector collector)
    {
        this.warningCollector = collector;
    }

    public void addWarningToCollector(WarningCode code, String warningMessage, AstNode node)
    {
        warningCollector.add(code, warningMessage, node);
    }

    /**
     * Entry point to recursive visiting routine. We recurse, add any warnings to the current collector, then return.
     *
     * @param node The root of the AST we're validating
     */
    public void lint(AstNode node)
    {
        node.jjtAccept(this, null);
    }

    public WarningCollector getWarningCollector()
    {
        return warningCollector;
    }
}
