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

package com.facebook.coresql.warning;

import com.google.common.collect.ImmutableList;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class DefaultWarningCollector
        implements WarningCollector
{
    private final Map<WarningCode, List<CoreSqlWarning>> warnings = new LinkedHashMap<>();
    private final WarningCollectorConfig config;
    private int numWarnings;
    private final WarningHandlingLevel warningHandlingLevel;

    public DefaultWarningCollector(WarningCollectorConfig config, WarningHandlingLevel warningHandlingLevel)
    {
        this.config = requireNonNull(config, "config is null");
        this.warningHandlingLevel = warningHandlingLevel;
    }

    @Override
    public boolean add(CoreSqlWarning warning)
    {
        requireNonNull(warning, "warning is null");
        boolean successfullyAdded = false;
        switch (warningHandlingLevel) {
            case SUPPRESS:
            case AS_ERROR:
                break;
            case NORMAL:
                successfullyAdded = addWarningIfNumWarningsLessThanConfig(warning);
        }
        return successfullyAdded;
    }

    @Override
    public List<List<CoreSqlWarning>> getWarnings()
    {
        return ImmutableList.copyOf(warnings.values());
    }

    @Override
    public void clearWarnings()
    {
        warnings.clear();
        numWarnings = 0;
    }

    private boolean addWarningIfNumWarningsLessThanConfig(CoreSqlWarning coreSqlWarning)
    {
        if (numWarnings < config.getMaxWarnings()) {
            warnings.putIfAbsent(coreSqlWarning.getWarningCode(), new LinkedList<>());
            warnings.get(coreSqlWarning.getWarningCode()).add(coreSqlWarning);
            numWarnings += 1;
            return true;
        }
        return false;
    }
}
