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

public enum StandardWarningCode
{
    MIXING_AND_OR_WITHOUT_PARENTHESES(0x0000_0001);

    private final WarningCode warningCode;

    StandardWarningCode(int code)
    {
        warningCode = new WarningCode(code, name());
    }

    public WarningCode getWarningCode()
    {
        return warningCode;
    }
}
