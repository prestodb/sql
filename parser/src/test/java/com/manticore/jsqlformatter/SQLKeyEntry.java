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
package com.manticore.jsqlformatter;

import java.io.File;
import java.util.Map;

public class SQLKeyEntry
        implements Map.Entry<File, String>
{
    File key;
    String value;

    public SQLKeyEntry(File key, String value)
    {
        this.key = key;
        this.value = value;
    }

    @Override
    public File getKey()
    {
        return key;
    }

    @Override
    public String getValue()
    {
        return value;
    }

    @Override
    public String setValue(String value)
    {
        this.value = value;
        return this.value;
    }

    @Override
    public String toString()
    {
        return value + " @ " + key.getName();
    }
}
