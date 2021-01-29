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

import org.testng.annotations.Test;

import static com.facebook.coresql.warning.StandardWarningCode.MIXING_AND_OR_WITHOUT_PARENTHESES;
import static com.facebook.coresql.warning.WarningHandlingLevel.NORMAL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestWarningCollector
{
    @Test
    public void addWarningTest()
    {
        int maxNumWarnings = 2;
        WarningCollectorConfig config = new WarningCollectorConfig();
        config.setMaxWarnings(maxNumWarnings);
        WarningCollector collector = new DefaultWarningCollector(new WarningCollectorConfig(), NORMAL);
        assertTrue(collector.add(new CoreSqlWarning(MIXING_AND_OR_WITHOUT_PARENTHESES.getWarningCode(), "", 0, 0, 0, 0)));
        assertEquals(1, collector.getWarnings().size());
    }
}
