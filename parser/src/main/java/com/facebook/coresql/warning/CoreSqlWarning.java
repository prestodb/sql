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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public final class CoreSqlWarning
{
    private final WarningCode warningCode;
    private final String message;
    private final WarningLocation location;

    public CoreSqlWarning(WarningCode warningCode, String message,
            int beginLine, int beginColumn, int endLine, int endColumn)
    {
        this.warningCode = requireNonNull(warningCode, "Warning code is null");
        this.message = requireNonNull(message, "Warning message is null");
        this.location = new WarningLocation(beginLine, beginColumn, endLine, endColumn);
    }

    public WarningCode getWarningCode()
    {
        return warningCode;
    }

    @Override
    public String toString()
    {
        return format("Code and Code Num: %s\n Msg: %s\n Location: %s", warningCode, message, location);
    }

    private static class WarningLocation
    {
        private final int beginLine;
        private final int beginColumn;
        private final int endLine;
        private final int endColumn;

        public WarningLocation(int beginLine, int beginColumn, int endLine, int endColumn)
        {
            this.beginLine = beginLine;
            this.beginColumn = beginColumn;
            this.endLine = endLine;
            this.endColumn = endColumn;
        }

        @Override
        public String toString()
        {
            return format("%d:%d-%d:%d", beginLine, beginColumn, endLine, endColumn);
        }
    }
}
