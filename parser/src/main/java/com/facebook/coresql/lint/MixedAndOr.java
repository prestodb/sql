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

import com.facebook.coresql.parser.AndExpression;
import com.facebook.coresql.parser.AstNode;
import com.facebook.coresql.parser.OrExpression;
import com.facebook.coresql.warning.CoreSqlWarning;
import com.facebook.coresql.warning.WarningCollector;

import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTANDEXPRESSION;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTOREXPRESSION;
import static com.facebook.coresql.warning.StandardWarningCode.MIXING_AND_OR_WITHOUT_PARENTHESES;

/**
 * A visitor that validates an AST built from an SQL string. Right now, it validates
 * a single clause: don't mix AND and OR without parentheses.
 */
public class MixedAndOr
        extends LintingVisitor
{
    private static final String WARNING_MESSAGE = "Mixing AND and OR without parentheses.";

    public MixedAndOr(WarningCollector collector)
    {
        super(collector);
    }

    /**
     * Entry point to recursive visiting routine. We recurse, add any warnings to the current collector, then return.
     *
     * @param node The root of the AST we're validating
     * @return isValidInput True iff the AST is valid (does not mix AND and OR w/o parentheses)
     */
    public void lint(AstNode node)
    {
        node.jjtAccept(this, null);
    }

    @Override
    public void visit(OrExpression node, Void data)
    {
        if (node.getFirstChildOfKind(JJTANDEXPRESSION) != null) {
            super.addWarningToCollector(
                    new CoreSqlWarning(MIXING_AND_OR_WITHOUT_PARENTHESES.getWarningCode(),
                            WARNING_MESSAGE,
                            node.beginToken.beginLine, node.beginToken.beginColumn, node.beginToken.endLine, node.beginToken.endColumn));
        }
        defaultVisit(node, data);
    }

    @Override
    public void visit(AndExpression node, Void data)
    {
        if (node.getFirstChildOfKind(JJTOREXPRESSION) != null) {
            super.addWarningToCollector(
                    new CoreSqlWarning(MIXING_AND_OR_WITHOUT_PARENTHESES.getWarningCode(),
                            WARNING_MESSAGE,
                            node.beginToken.beginLine, node.beginToken.beginColumn, node.beginToken.endLine, node.beginToken.endColumn));
        }
        defaultVisit(node, data);
    }
}
