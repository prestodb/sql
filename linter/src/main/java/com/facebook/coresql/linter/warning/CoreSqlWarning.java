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

import com.facebook.coresql.parser.Location;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public final class CoreSqlWarning
{
    private final WarningCode warningCode;
    private final String message;
    private final Location location;

    public CoreSqlWarning(WarningCode warningCode, String message,
            int beginLine,
            int beginColumn,
            int endLine,
            int endColumn)
    {
        this.warningCode = requireNonNull(warningCode, "Warning code is null");
        this.message = requireNonNull(message, "Warning message is null");
        this.location = new Location(beginLine, beginColumn, endLine, endColumn);
    }

    public WarningCode getWarningCode()
    {
        return warningCode;
    }

    @Override
    public String toString()
    {
        return format("Warning@%s message: %s [%s]", location, message, warningCode);
    }
}
