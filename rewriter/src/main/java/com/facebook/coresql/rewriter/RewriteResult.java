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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

public class RewriteResult
{
    private String nameOfRewrite;
    private String originalSql;
    private String rewrittenSql;

    @JsonCreator
    public RewriteResult(
            @JsonProperty("nameOfRewrite") String nameOfRewrite,
            @JsonProperty("originalSql") String originalSql,
            @JsonProperty("rewrittenSql") String rewrittenSql)
    {
        this.nameOfRewrite = requireNonNull(nameOfRewrite, "name of rewrite is null");
        this.originalSql = requireNonNull(originalSql, "original sql statement is null");
        this.rewrittenSql = requireNonNull(rewrittenSql, "rewritten sql statement is null");
    }

    @JsonProperty("nameOfRewrite")
    public String getNameOfRewrite()
    {
        return nameOfRewrite;
    }

    @JsonProperty("originalSql")
    public String getOriginalSql()
    {
        return originalSql;
    }

    @JsonProperty("rewrittenSql")
    public String getRewrittenSql()
    {
        return rewrittenSql;
    }
}
