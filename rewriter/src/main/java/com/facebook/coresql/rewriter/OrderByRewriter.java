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
import com.facebook.coresql.parser.OrderByClause;
import com.facebook.coresql.parser.QuerySpecification;
import com.facebook.coresql.parser.SimpleNode;
import com.facebook.coresql.parser.SqlParserDefaultVisitor;
import com.facebook.coresql.parser.Subquery;
import com.facebook.coresql.parser.Unparser;

import java.util.HashSet;
import java.util.Set;

import static com.facebook.coresql.parser.ParserHelper.parseStatement;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTINSERT;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTLIMITCLAUSE;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTORDERBYCLAUSE;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTTABLEDEFINITION;
import static java.util.Objects.requireNonNull;

public class OrderByRewriter
        extends Rewriter
{
    PatternMatcher<Set<AstNode>> matcher;
    public Set<AstNode> patternMatchedNodes;
    private static final String REWRITE_NAME = "ORDER BY without LIMIT";

    public OrderByRewriter()
    {
        this.matcher = new OrderByPatternMatcher();
        this.patternMatchedNodes = new HashSet<>();
    }

    @Override
    public boolean rewritePatternIsPresent(String sql)
    {
        AstNode root = requireNonNull(parseStatement(sql));
        patternMatchedNodes = matcher.matchPattern(root);
        return !patternMatchedNodes.isEmpty();
    }

    @Override
    public RewriteResult rewrite(String originalSql)
    {
        AstNode root = requireNonNull(parseStatement(originalSql));
        patternMatchedNodes = matcher.matchPattern(root);
        String rewrittenSql = Unparser.unparse(root, this);
        return new RewriteResult(REWRITE_NAME, originalSql, rewrittenSql);
    }

    @Override
    public void visit(OrderByClause node, Void data)
    {
        if (patternMatchedNodes.contains(node)) {
            unparseUpto(node);
            moveToEndOfNode(node);
        }
    }

    private static class OrderByPatternMatcher
            extends SqlParserDefaultVisitor
            implements PatternMatcher<Set<AstNode>>
    {
        private final Set<AstNode> matchedNodes;
        private int depth;
        private static final int MINIMUM_SUBQUERY_DEPTH = 2; // Past this depth, all queries we encounter are subqueries

        public OrderByPatternMatcher()
        {
            this.matchedNodes = new HashSet<>();
        }

        @Override
        public Set<AstNode> matchPattern(AstNode node)
        {
            requireNonNull(node, "AST passed to pattern matcher was null");
            node.jjtAccept(this, null);
            return matchedNodes;
        }

        /**
         * Checks for a specific pattern in the AST this pattern matcher is traversing:
         * 1. Query has an ORDER BY
         * 2. Query does not have a LIMIT
         * 3. The query is a subquery OR the result of the query is used to CREATE a table or INSERT into a table
         *
         * @param node The node we're currently visiting
         * @return True if the pattern is matched at the current node, else false
         */
        private boolean hasOrderByWithNoLimit(AstNode node)
        {
            if (depth > MINIMUM_SUBQUERY_DEPTH) {
                return hasOrderByAndDoesNotHaveLimit(node);
            }
            return hasOrderByAndDoesNotHaveLimit(node) && isUsedInInsertOrCreate(node); // Non-subquery check
        }

        private boolean hasOrderByAndDoesNotHaveLimit(AstNode node)
        {
            return node.hasChildOfKind(JJTORDERBYCLAUSE) && !node.hasChildOfKind(JJTLIMITCLAUSE);
        }

        private boolean isUsedInInsertOrCreate(AstNode node)
        {
            return (node.jjtGetParent().getId() == JJTTABLEDEFINITION || node.jjtGetParent().getId() == JJTINSERT);
        }

        @Override
        public void defaultVisit(SimpleNode node, Void data)
        {
            ++depth;
            node.childrenAccept(this, data);
            --depth;
        }

        @Override
        public void visit(QuerySpecification node, Void data)
        {
            if (hasOrderByWithNoLimit(node)) {
                matchedNodes.add(node.GetFirstChildOfKind(JJTORDERBYCLAUSE));
            }
            defaultVisit(node, data);
        }

        @Override
        public void visit(Subquery node, Void data)
        {
            if (hasOrderByWithNoLimit(node)) {
                matchedNodes.add(node.GetFirstChildOfKind(JJTORDERBYCLAUSE));
            }
            defaultVisit(node, data);
        }
    }
}
