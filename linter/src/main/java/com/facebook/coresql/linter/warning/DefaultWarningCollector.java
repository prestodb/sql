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

package com.facebook.coresql.linter.warning;

import com.facebook.coresql.parser.AstNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class DefaultWarningCollector
        implements WarningCollector
{
    private final Multimap<WarningCode, CoreSqlWarning> warnings = LinkedListMultimap.create();
    private final WarningCollectorConfig config;

    public DefaultWarningCollector(WarningCollectorConfig config)
    {
        this.config = requireNonNull(config, "config is null");
    }

    @Override
    public void add(WarningCode code, String warningMessage, AstNode node)
    {
        requireNonNull(code, "warning code is null");
        requireNonNull(warningMessage, "warning message is null");
        requireNonNull(node, "node is null");
        addWarningIfNumWarningsLessThanConfig(new CoreSqlWarning(code,
                warningMessage,
                node.beginToken.beginLine,
                node.beginToken.beginColumn,
                node.beginToken.endLine,
                node.beginToken.endColumn));
    }

    @Override
    public List<CoreSqlWarning> getAllWarnings()
    {
        return ImmutableList.copyOf(warnings.values());
    }

    @Override
    public void clearWarnings()
    {
        warnings.clear();
    }

    private void addWarningIfNumWarningsLessThanConfig(CoreSqlWarning coreSqlWarning)
    {
        if (warnings.size() < config.getMaxWarnings()) {
            warnings.put(coreSqlWarning.getWarningCode(), coreSqlWarning);
        }
    }
}
