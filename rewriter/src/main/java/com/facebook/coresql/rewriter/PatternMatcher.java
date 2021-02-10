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

public interface PatternMatcher<T>
{
    /**
     * Searches for a pattern in an Abstract Syntax Tree (AST).
     *
     * @param root root of the AST
     * @return T An arbitrary data structure containing information found during the pattern matching routine
     * Example: T is a Set<AstNode> which contains nodes that match the given pattern.
     */
    T matchPattern(AstNode root);
}
