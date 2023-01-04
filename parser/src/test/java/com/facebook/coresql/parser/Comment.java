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
package com.facebook.coresql.parser;

public class Comment
        implements Comparable<Comment>
{
    protected boolean newLine;
    protected boolean extraNewLine;
    protected int absolutePosition;
    protected int relativePosition;
    protected String text;

    public Comment(int absolutePosition, String text)
    {
        this.absolutePosition = absolutePosition;
        this.text = text;
    }

    @Override
    public int compareTo(Comment o)
    {
        return Integer.compare(absolutePosition, o.absolutePosition);
    }

    @Override
    public String toString()
    {
        return text;
    }
}
