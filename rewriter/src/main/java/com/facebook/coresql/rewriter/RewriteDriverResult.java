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
import com.google.common.collect.ImmutableList;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class RewriteDriverResult
{
    private final List<RewriteResult> rewriteResults;
    private final AstNode rewrittenSqlAst;

    public RewriteDriverResult(List<RewriteResult> rewriteResults, AstNode rewrittenSqlAsAst)
    {
        this.rewriteResults = requireNonNull(rewriteResults, "list of rewrite results is null");
        this.rewrittenSqlAst = requireNonNull(rewrittenSqlAsAst, "rewritten sql ast is null");
    }

    public List<RewriteResult> getRewriteResults()
    {
        return ImmutableList.copyOf(rewriteResults);
    }

    public AstNode getRewrittenSqlAsAst()
    {
        return rewrittenSqlAst;
    }
}
