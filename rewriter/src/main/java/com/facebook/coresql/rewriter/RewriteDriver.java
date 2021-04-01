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

import com.facebook.airlift.bootstrap.Bootstrap;
import com.facebook.airlift.log.Logger;
import com.facebook.coresql.parser.AstNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.facebook.coresql.parser.ParserHelper.parseStatement;
import static java.util.Objects.requireNonNull;

public class RewriteDriver
{
    private static final Logger LOG = Logger.get(RewriteDriver.class);
    private static final Set<Class<? extends Rewriter>> KNOWN_REWRITERS = ImmutableSet.of(OrderByRewriter.class);
    private final Set<Class<? extends Rewriter>> userEnabledRewriters;
    private AstNode root;

    public RewriteDriver(RewriteDriverConfig config, AstNode root)
    {
        this.root = requireNonNull(root, "AST given to driver was null");
        this.userEnabledRewriters = config.getUserEnabledRewriters();
        new Bootstrap(new RewriteDriverModule()).doNotInitializeLogging().quiet().initialize();
    }

    public Optional<RewriteDriverResult> applyRewriters()
    {
        ImmutableList.Builder<RewriteResult> builder = ImmutableList.builder();
        for (Class<? extends Rewriter> rewriter : userEnabledRewriters) {
            if (!KNOWN_REWRITERS.contains(rewriter)) {
                LOG.error("An unknown rewriter was passed to rewrite driver: %s", rewriter.getName());
                return Optional.empty();
            }
            Rewriter rewriterInstance;
            try {
                rewriterInstance = rewriter.getConstructor(AstNode.class).newInstance(root);
            }
            catch (Exception e) {
                LOG.error(e, "Exception thrown while creating an instance of this rewriter: %s", rewriter.getName());
                continue;
            }
            Optional<RewriteResult> result = rewriterInstance.rewrite();
            if (result.isPresent()) {
                builder.add(result.get());
                root = parseStatement(result.get().getRewrittenSql());
            }
        }
        List<RewriteResult> results = builder.build();
        return results.isEmpty() ? Optional.empty() : Optional.of(new RewriteDriverResult(results, root));
    }
}
