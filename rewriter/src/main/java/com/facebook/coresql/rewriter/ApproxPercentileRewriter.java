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
import com.facebook.coresql.parser.FunctionCall;
import com.facebook.coresql.parser.SqlParserDefaultVisitor;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.Formatter;
import java.util.Optional;

import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTARGUMENTLIST;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTIDENTIFIER;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTUNSIGNEDNUMERICLITERAL;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class ApproxPercentileRewriter
        extends Rewriter
{
    private static final int ZERO_INDEXING_OFFSET = 1;
    private static final String REPLACEMENT_FORMAT = "(%s, ARRAY[%s])[%d]";
    private static final String REWRITE_NAME = "Multiple APPROX PERCENTILE with same first arg and literal second arg";
    private static final String APPROX_PERCENTILE_IDENTIFIER = "APPROX_PERCENTILE";
    private final AstNode root;
    private final ListMultimap<String, AstNode> firstArgToApproxPercentileNode; // A map of String to the APPROX_PERCENTILE nodes with that String as their first argument
    private final ListMultimap<String, AstNode> firstArgToPercentiles; // A map of first argument to the node that contains the percentiles corresponding to that first argument

    public ApproxPercentileRewriter(AstNode root)
    {
        this.root = requireNonNull(root, "AST passed to rewriter was null");
        ApproxPercentilePatternMatcherResult patternMatcherResult = new ApproxPercentilePatternMatcher(root).matchPattern();
        this.firstArgToApproxPercentileNode = patternMatcherResult.getFirstArgToApproxPercentileNode();
        this.firstArgToPercentiles = patternMatcherResult.getFirstArgToPercentiles();
    }

    @Override
    public Optional<RewriteResult> rewrite()
    {
        if (!approxPercentileRewritePatternIsPresent()) {
            return Optional.empty();
        }
        String rewrittenSql = unparse(root, this);
        return Optional.of(new RewriteResult(REWRITE_NAME, rewrittenSql));
    }

    @Override
    public void visit(FunctionCall node, Void data)
    {
        if (needsApproxPercentileRewrite(node)) {
            applyApproxPercentileRewrite(node);
        }
        else {
            defaultVisit(node, data);
        }
    }

    private boolean approxPercentileRewritePatternIsPresent()
    {
        return firstArgToApproxPercentileNode.keySet().stream()
                .anyMatch(key -> firstArgToApproxPercentileNode.get(key).size() >= 2);
    }

    private boolean needsApproxPercentileRewrite(FunctionCall node)
    {
        return firstArgToApproxPercentileNode.containsValue(node) && firstArgToApproxPercentileNode.get(unparse(getNthArgument(node, 0).get()).trim()).size() >= 2;
    }

    private String getPercentilesAsString(String firstArg)
    {
        String percentilesAsString = firstArgToPercentiles.get(firstArg).stream()
                .map(node -> unparse(node).trim())
                .collect(toList())
                .toString();
        return percentilesAsString.substring(1, percentilesAsString.length() - 1);
    }

    /**
     * Generates a rewritten version of the current subtree.
     *
     * @param node The function call node we're rewriting
     */
    private void applyApproxPercentileRewrite(AstNode node)
    {
        // First, unparse up to the node's last child. This ensures we don't miss any special tokens
        unparseUpto(node.LastChild());
        // Then, add the rewritten version to the rewriter's result object (i.e. stringBuilder)
        String firstArg = unparse(getNthArgument(node, 0).get()).trim();
        AstNode secondArg = getNthArgument(node, 1).get();
        Formatter formatter = new Formatter(stringBuilder);
        formatter.format(REPLACEMENT_FORMAT, firstArg, getPercentilesAsString(firstArg), getIndexOfPercentile(firstArg, secondArg));
        // Lastly, move to end of this node -- we've already added a rewritten version of it to the result, so we don't need to process it further
        moveToEndOfNode(node);
    }

    private int getIndexOfPercentile(String firstArg, AstNode secondArg)
    {
        return firstArgToPercentiles.get(firstArg).indexOf(secondArg) + ZERO_INDEXING_OFFSET;
    }

    private static Optional<AstNode> getNthArgument(AstNode node, int n)
    {
        Optional<AstNode> argList = Optional.ofNullable(node.GetFirstChildOfKind(JJTARGUMENTLIST));
        if (!argList.isPresent() || argList.get().jjtGetNumChildren() < n) {
            return Optional.empty();
        }
        AstNode nthArg = (AstNode) argList.get().jjtGetChild(n);
        return Optional.of(nthArg);
    }

    private static boolean hasUnsignedLiteralSecondArg(AstNode node)
    {
        return getNthArgument(node, 1).filter(astNode -> astNode.getId() == JJTUNSIGNEDNUMERICLITERAL).isPresent();
    }

    private static boolean isApproxPercentileNode(AstNode node)
    {
        Optional<AstNode> identifier = Optional.ofNullable(node.GetFirstChildOfKind(JJTIDENTIFIER));
        if (!identifier.isPresent()) {
            return false;
        }
        Optional<String> image = Optional.ofNullable(identifier.get().GetImage());
        return image.isPresent() && image.get().equalsIgnoreCase(APPROX_PERCENTILE_IDENTIFIER);
    }

    private static class ApproxPercentilePatternMatcher
            extends SqlParserDefaultVisitor
    {
        private final AstNode root;
        private final ImmutableListMultimap.Builder<String, AstNode> firstArgToApproxPercentileNode = ImmutableListMultimap.builder();
        private final ImmutableListMultimap.Builder<String, AstNode> firstArgToPercentiles = ImmutableListMultimap.builder();

        public ApproxPercentilePatternMatcher(AstNode root)
        {
            this.root = requireNonNull(root, "AST passed to pattern matcher was null");
        }

        public ApproxPercentilePatternMatcherResult matchPattern()
        {
            root.jjtAccept(this, null);
            return new ApproxPercentilePatternMatcherResult(firstArgToApproxPercentileNode.build(), firstArgToPercentiles.build());
        }

        @Override
        public void visit(FunctionCall node, Void data)
        {
            if (isApproxPercentileNode(node) && hasUnsignedLiteralSecondArg(node)) {
                String firstArg = unparse(getNthArgument(node, 0).get()).trim();
                AstNode secondArg = getNthArgument(node, 1).get();
                firstArgToApproxPercentileNode.put(firstArg, node);
                firstArgToPercentiles.put(firstArg, secondArg);
            }
            defaultVisit(node, data);
        }
    }

    private static class ApproxPercentilePatternMatcherResult
    {
        private final ListMultimap<String, AstNode> firstArgToApproxPercentileNode;
        private final ListMultimap<String, AstNode> firstArgToPercentiles;

        public ApproxPercentilePatternMatcherResult(ListMultimap<String, AstNode> firstArgToApproxPercentileNode,
                ListMultimap<String, AstNode> firstArgToPercentiles)
        {
            this.firstArgToApproxPercentileNode = requireNonNull(firstArgToApproxPercentileNode);
            this.firstArgToPercentiles = requireNonNull(firstArgToPercentiles);
        }

        public ListMultimap<String, AstNode> getFirstArgToApproxPercentileNode()
        {
            return firstArgToApproxPercentileNode;
        }

        public ListMultimap<String, AstNode> getFirstArgToPercentiles()
        {
            return firstArgToPercentiles;
        }
    }
}
