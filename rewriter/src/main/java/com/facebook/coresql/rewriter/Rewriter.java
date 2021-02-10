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

import com.facebook.coresql.parser.Unparser;

public abstract class Rewriter
        extends Unparser
{
    /**
     * Attempts to rewrite a SQL statement, storing the name of the rewriter, original string, and rewritten string in a RewriteResult object.
     *
     * @param sql The statement that will be rewritten
     * @return A RewriteResult object containing the name of the rewriter, original string, and rewritten string
     */
    public abstract RewriteResult rewrite(String sql);

    /**
     * Checks if the pattern we're trying to rewrite is present within a SQL statement.
     *
     * @param sql The statement we're checking
     * @return true if the rewrite pattern is present else false
     */
    public abstract boolean rewritePatternIsPresent(String sql);
}
