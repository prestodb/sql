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
import com.facebook.coresql.parser.SetOperation;
import com.facebook.coresql.parser.SqlParserDefaultVisitor;
import com.facebook.coresql.parser.Unparser;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.facebook.coresql.parser.SqlParserConstants.ALL;
import static com.facebook.coresql.parser.SqlParserConstants.AND;
import static com.facebook.coresql.parser.SqlParserConstants.CASE;
import static com.facebook.coresql.parser.SqlParserConstants.END;
import static com.facebook.coresql.parser.SqlParserConstants.FROM;
import static com.facebook.coresql.parser.SqlParserConstants.SELECT;
import static com.facebook.coresql.parser.SqlParserConstants.THEN;
import static com.facebook.coresql.parser.SqlParserConstants.TRUE;
import static com.facebook.coresql.parser.SqlParserConstants.UNION;
import static com.facebook.coresql.parser.SqlParserConstants.WHEN;
import static com.facebook.coresql.parser.SqlParserConstants.WHERE;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTALIAS;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTFROMCLAUSE;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTGROUPBYCLAUSE;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTHAVINGCLAUSE;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTSELECT;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTSELECTLIST;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTTABLEEXPRESSION;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTTABLENAME;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTWHERECLAUSE;
import static java.util.Objects.requireNonNull;

public class UnionAllToDisjunction
        extends Rewriter
{
    private static final String REWRITE_NAME = "UNION ALL of same dataset to ORT";
    private static final String UNNEST = "CROSS JOIN UNNEST(SEQUENCE(%s, %s)) AS UNNEST__(index__)";

    private final AstNode root;
    private final Set<AstNode> matchedNodes;
    private final PatternMatcher patternMatcher;

    public UnionAllToDisjunction(AstNode root)
    {
        this.root = requireNonNull(root, "AST passed to rewriter was null");
        this.patternMatcher = new PatternMatcher(root);
        this.matchedNodes = patternMatcher.matchPattern();
    }

    @Override
    public Optional<RewriteResult> rewrite()
    {
        if (matchedNodes.isEmpty()) {
            return Optional.empty();
        }

        String rewrittenSql = Unparser.unparseClean(root, this);
        return Optional.of(new RewriteResult(REWRITE_NAME, rewrittenSql));
    }

    private void mergeUionAllChildren(String from, List<AstNode> selectClauses)
    {
        printKeyword(SELECT);
        AstNode firstSelect = selectClauses.get(0);
        int selectItemCount = firstSelect.GetFirstChildOfKind(JJTSELECTLIST).NumChildren();

        for (int i = 0; i < selectItemCount; i++) {
            if (i > 0) {
                printToken(",");
            }

            int selectIndex = 1;
            printKeyword(CASE);
            printToken("index__");

            for (AstNode select : selectClauses) {
                AstNode selectList = select.GetFirstChildOfKind(JJTSELECTLIST);
                printKeyword(WHEN);
                printToken(String.valueOf(selectIndex));
                printKeyword(THEN);
                AstNode expression = selectList.GetChild(i).GetChild(0);
                // Just unparse the select expression excluing the alias
                printToken(unparseClean(expression));
                selectIndex++;
            }

            printKeyword(END);
        }

        if (from != null) {
            // Just generate the source here
            printToken(unparseClean(firstSelect.GetFirstChildOfKind(JJTTABLEEXPRESSION).GetFirstChildOfKind(JJTFROMCLAUSE)));
        }
        else {
            // Generate a dummy select 1
            printToken("(");
            printKeyword(SELECT);
            printToken("1");
            printToken(")");
        }

        // Generate a lateral join
        printToken(String.format(UNNEST, 1, selectClauses.size()));

        // Now generate the where clauses with an OR
        boolean first = true;
        printKeyword(WHERE);
        printToken("(");
        for (AstNode select : selectClauses) {
            AstNode tableExpression = select.GetFirstChildOfKind(JJTTABLEEXPRESSION);
            if (tableExpression.NumChildren() == 2) {
                AstNode whereClause = tableExpression.GetFirstChildOfKind(JJTWHERECLAUSE);
                if (!first) {
                    printToken(" OR ");
                }
                else {
                    first = false;
                }

                printToken("(");
                printToken(unparseClean(whereClause.GetChild(0)));
                printToken(")");
            }
        }

        printToken(")");

        if (first) {
            printKeyword(TRUE);
        }

        // Now we generate the specific predicate for each of the branches
        printKeyword(AND);
        printToken("(");
        printKeyword(CASE);
        printToken("index__");
        int caseIndex = 1;
        for (AstNode select : selectClauses) {
            AstNode tableExpression = select.GetFirstChildOfKind(JJTTABLEEXPRESSION);
            printKeyword(WHEN);
            printToken(String.valueOf(caseIndex++));
            printKeyword(THEN);
            if (tableExpression.NumChildren() == 2) {
                AstNode whereClause = tableExpression.GetFirstChildOfKind(JJTWHERECLAUSE);
                printToken("(");
                printToken(unparseClean(whereClause.GetChild(0)));
                printToken(")");
            }
            else {
                // Missing where just true
                printKeyword(TRUE);
            }
        }
        printKeyword(END);
        printToken(")");
    }

    private void rewriteUnionBranches(AstNode union)
    {
        Map<String, List<AstNode>> repeatedSources = patternMatcher.getRepeatedSources(union);

        // Collect the select aliases from the first branch
        ImmutableList.Builder<String> finalSelects = ImmutableList.builder();
        AstNode firstBranch = union.GetChild(0);
        AstNode selectList = firstBranch.GetFirstChildOfKind(JJTSELECTLIST);
        for (int i = 0; i < selectList.NumChildren(); i++) {
            AstNode item = selectList.GetChild(i);
            AstNode alias = item.GetFirstChildOfKind(JJTALIAS);
            if (alias == null) {
                // TODO(kaikalur): Use a method to make it uniq
                finalSelects.add("\"" + item.GetChild(0).GetSqlString() .trim() + "\"");
            }
            else {
                // Simple hack for simple ids
                finalSelects.add(alias.GetChild(0).GetSqlString());
            }
        }

        // We generate an outer select for ease of aliasing:
        printKeyword(SELECT);
        printToken("*");

        printKeyword(FROM);
        printToken("(");

        boolean first = true;
        // Now the real thing
        for (Map.Entry<String, List<AstNode>> entry : repeatedSources.entrySet()) {
            if (!first) {
                printKeyword(UNION);
                printKeyword(ALL);
            }
            else {
                first = false;
            }

            // The real rewrite
            mergeUionAllChildren(entry.getKey(), entry.getValue());
        }

        // TODO(sreeni): Generate unique table alias
        printToken(") AS t__ (");
        first = true;
        for (String s : finalSelects.build()) {
            if (!first) {
                printToken(",");
            }
            else {
                first = false;
            }
            printToken(s);
        }
        printToken(")");
    }

    @Override
    public void visit(SetOperation node, Void data)
    {
        if (!matchedNodes.contains(node)) {
            defaultVisit(node, data);
            return;
        }

        // Unparse upto the first token
        unparseUpto(node);

        // Rewrite to avoid repeated scans
        rewriteUnionBranches(node);

        // Move to the end of the node
        moveToEndOfNode(node);
    }

    private static class PatternMatcher
            extends SqlParserDefaultVisitor
    {
        private final AstNode root;
        private final ImmutableSet.Builder<AstNode> builder = ImmutableSet.builder();
        private final ImmutableMap.Builder<AstNode, Map<String, List<AstNode>>> repeatedSourcesBuilder = ImmutableMap.builder();
        private static final int MINIMUM_SUBQUERY_DEPTH = 2; // Past this depth, all queries we encounter are subqueries

        public PatternMatcher(AstNode root)
        {
            this.root = requireNonNull(root, "AST passed to rewriter was null");
        }

        public Set<AstNode> matchPattern()
        {
            root.jjtAccept(this, null);
            repeatedSourcesBuilder.build();
            return builder.build();
        }

        private String getSourceNode(AstNode select)
        {
            // TODO(kaikalur): mvoe this to ASTUtils

            if (select.Kind() == JJTSELECT && select.GetChild(1).Kind() == JJTTABLEEXPRESSION) {
                AstNode tableExpression = select.GetFirstChildOfKind(JJTTABLEEXPRESSION);
                if (tableExpression.NumChildren() == 1 || (tableExpression.NumChildren() == 2 && tableExpression.GetChild(1).Kind() == JJTWHERECLAUSE)) {
                    AstNode from = tableExpression.GetFirstChildOfKind(JJTFROMCLAUSE);
                    // See that it is a simple select/filter
                    if (from.GetChild(0).Kind() == JJTTABLENAME &&
                            !(select.hasChildOfKind(JJTGROUPBYCLAUSE) || select.hasChildOfKind(JJTHAVINGCLAUSE))) {
                        // like FROM T
                        return from.GetChild(0).GetSqlString();
                    }
                }
            }

            return null;
        }

        @Override
        public void visit(SetOperation setOperation, Void data)
        {
            if (true) { // (setOperation.beginToken.kind == UNION && setOperation.beginToken.next.kind == ALL) {
                boolean repeated = true;
                Map<String, List<AstNode>> selectsBySource = new HashMap<>();
                // Union all.
                for (int i = 0; i < setOperation.jjtGetNumChildren(); i++) {
                    AstNode select = setOperation.GetChild(i);
                    String source = getSourceNode(select);
                    List<AstNode> selects = selectsBySource.get(source);
                    if (selects == null) {
                        selects = new ArrayList<>();
                        selectsBySource.put(source, selects);
                    }
                    else {
                        // Has some repeated sources
                        repeated = true;
                    }
                    selects.add(select);
                }

                // For now we support all branches having the same table.
                if (repeated) {
                    builder.add(setOperation);
                    repeatedSourcesBuilder.put(setOperation, selectsBySource);
                }
            }

            defaultVisit(setOperation, data);
        }

        private Map<String, List<AstNode>> getRepeatedSources(AstNode setOperation)
        {
            Map<AstNode, Map<String, List<AstNode>>> repeatedSources = repeatedSourcesBuilder.build();
            return repeatedSources.get(setOperation);
        }
    }
}
