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

import com.facebook.coresql.parser.AggregationFunction;
import com.facebook.coresql.parser.AstNode;
import com.facebook.coresql.parser.Comparison;
import com.facebook.coresql.parser.SqlParserDefaultVisitor;
import com.google.common.collect.ImmutableSet;

import java.util.Optional;
import java.util.Set;

import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTAGGREGATIONFUNCTION;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTIDENTIFIER;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTSETQUANTIFIER;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class CountDistinctRewriter
        extends Rewriter
{
    private final AstNode root;
    private final Set<AstNode> patternMatchedNodes;
    private static final String REPLACEMENT = "MIN(%s) IS DISTINCT FROM MAX(%s)";
    private static final String REWRITE_NAME = "Duplicate Check Using COUNT(DISTINCT x) > 1";

    public CountDistinctRewriter(AstNode root)
    {
        this.root = requireNonNull(root, "AST passed to rewriter was null");
        this.patternMatchedNodes = new CountDistinctPatternMatcher(root).matchPattern();
    }

    @Override
    public Optional<RewriteResult> rewrite()
    {
        if (patternMatchedNodes.isEmpty()) {
            return Optional.empty();
        }
        String rewrittenSql = unparse(root, this);
        return Optional.of(new RewriteResult(REWRITE_NAME, rewrittenSql));
    }

    @Override
    public void visit(Comparison node, Void data)
    {
        if (patternMatchedNodes.contains(node)) {
            applyCountDistinctRewrite(node);
        }
        else {
            defaultVisit(node, data);
        }
    }

    private void applyCountDistinctRewrite(Comparison node)
    {
        // First, unparse up to the node. This ensures we don't miss any special tokens
        unparseUpto(node);
        // Then, add the rewritten version to the Unparser
        String identifier = unparse(node.GetFirstChildOfKind(JJTAGGREGATIONFUNCTION).GetFirstChildOfKind(JJTIDENTIFIER)).trim();
        printToken(format(REPLACEMENT, identifier, identifier));
        // Move to end of this node -- we've already put in a rewritten version of it, so we don't need to unparse it
        moveToEndOfNode(node);
    }

    private static class CountDistinctPatternMatcher
            extends SqlParserDefaultVisitor
    {
        private final AstNode root;
        private final ImmutableSet.Builder<AstNode> builder = ImmutableSet.builder();

        public CountDistinctPatternMatcher(AstNode root)
        {
            this.root = requireNonNull(root, "AST passed to pattern matcher was null");
        }

        public Set<AstNode> matchPattern()
        {
            root.jjtAccept(this, null);
            return builder.build();
        }

        private boolean secondArgIsLiteralOne(Comparison node)
        {
            Optional<AstNode> secondArg = Optional.ofNullable((AstNode) node.jjtGetChild(1));
            return secondArg.isPresent() && unparse(secondArg.get()).trim().equals("1");
        }

        private boolean aggregationHasCountDistinct(AggregationFunction node)
        {
            Optional<AstNode> setQuantifier = Optional.ofNullable(node.GetFirstChildOfKind(JJTSETQUANTIFIER));
            return setQuantifier.isPresent() && node.beginToken.image.equalsIgnoreCase("COUNT") && unparse(setQuantifier.get()).equalsIgnoreCase("DISTINCT");
        }

        private boolean isUsingCountDistinctComparisonToCheckUniqueness(Comparison node)
        {
            Optional<AstNode> aggregationFunction = Optional.ofNullable(node.GetFirstChildOfKind(JJTAGGREGATIONFUNCTION));
            return aggregationFunction.isPresent() && aggregationHasCountDistinct((AggregationFunction) aggregationFunction.get()) && secondArgIsLiteralOne(node);
        }

        @Override
        public void visit(Comparison node, Void data)
        {
            if (isUsingCountDistinctComparisonToCheckUniqueness(node)) {
                builder.add(node);
            }
            defaultVisit(node, data);
        }
    }
}
