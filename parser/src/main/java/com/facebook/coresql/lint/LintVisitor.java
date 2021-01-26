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
import com.facebook.coresql.parser.SimpleNode;
import com.facebook.coresql.parser.SqlParserDefaultVisitor;
import com.facebook.coresql.warning.CoreSqlWarning;
import com.facebook.coresql.warning.WarningCollector;

import java.util.List;

import static com.facebook.coresql.warning.StandardWarningCode.MIXING_AND_OR_WITHOUT_PARENTHESES;

/**
 * A visitor that validates an AST built from an SQL string. Right now, it validates
 * a single clause: don't mix AND and OR without parentheses.
 */
public class LintVisitor
        extends SqlParserDefaultVisitor
{
    public WarningCollector warningCollector;

    public LintVisitor(WarningCollector collector)
    {
        this.warningCollector = collector;
    }

    /**
     * Entry point to recursive visiting routine. We recurse, find any warning, then return the warnings found.
     *
     * @param node The root of the AST we're validating
     * @param visitor The visitor instance that will traverse the AST
     * @return isValidInput True iff the AST is valid (does not mix AND and OR w/o parentheses)
     */
    public static List<CoreSqlWarning> lint(AstNode node, LintVisitor visitor)
    {
        node.jjtAccept(visitor, null);
        return visitor.warningCollector.getWarnings();
    }

    /**
     * The entry point for external calls.
     *
     * @param node The root of the AST we're validating
     * @return boolean True iff the AST is valid (does not mix AND and OR w/o parentheses)
     */
    public static List<CoreSqlWarning> lint(AstNode node, WarningCollector collector)
    {
        return lint(node, new LintVisitor(collector));
    }

    /**
     * Recursively visits all children nodes in pre-order/DFS.
     *
     * @param node The node we're currently visiting
     * @param data
     */
    @Override
    public void defaultVisit(SimpleNode node, Void data)
    {
        node.childrenAccept(this, data);
    }

    @Override
    public void visit(OrExpression node, Void data)
    {
        // AndExpression kind is 57
        if (node.getFirstChildOfKind(57) != null) {
            warningCollector.add(new CoreSqlWarning(MIXING_AND_OR_WITHOUT_PARENTHESES, "To reduce ambiguity, don't mix AND and OR without parentheses."));
        }
        defaultVisit(node, data);
    }

    @Override
    public void visit(AndExpression node, Void data)
    {
        // OrExpression kind is 56
        if (node.getFirstChildOfKind(56) != null) {
            warningCollector.add(new CoreSqlWarning(MIXING_AND_OR_WITHOUT_PARENTHESES, "To reduce ambiguity, don't mix AND and OR without parentheses."));
        }
        defaultVisit(node, data);
    }
}
