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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.facebook.coresql.parser.ParserHelper.parseStatement;
import static com.facebook.coresql.parser.SqlParserConstants.MAX_NON_RESERVED_WORD;
import static com.facebook.coresql.parser.SqlParserConstants.MAX_RESERVED_WORD;
import static com.facebook.coresql.parser.SqlParserConstants.MIN_NON_RESERVED_WORD;
import static com.facebook.coresql.parser.SqlParserConstants.MIN_RESERVED_WORD;
import static com.facebook.coresql.parser.SqlParserConstants.tokenImage;

public class TestKeywords
{
    private static String getTokenImageWithoutQuotes(int kind)
    {
        return tokenImage[kind].substring(1, tokenImage[kind].length() - 1);
    }

    @Test
    public void testReservedWords()
    {
        for (int i = MIN_RESERVED_WORD + 1; i < MAX_RESERVED_WORD; i++) {
            Assertions.assertNull(
                    parseStatement("select 1 as " + getTokenImageWithoutQuotes(i) + ";"),
                    getTokenImageWithoutQuotes(i) + " should be a reserved word.");
        }
    }

    @Test
    public void testNonReservedWords()
    {
        // The last one is a weird one - "COUNT" so we ignore it for now.
        for (int i = MIN_NON_RESERVED_WORD + 1; i < MAX_NON_RESERVED_WORD - 1; i++) {
            Assertions.assertNotNull(
                    parseStatement("select 1 as " + getTokenImageWithoutQuotes(i) + ";"),
                    getTokenImageWithoutQuotes(i) + " should NOT be a reserved word.");
        }
    }

    @Test
    public void testNoExtraneousKeywords()
    {
        for (int i = MAX_RESERVED_WORD + 1; i < tokenImage.length; i++) {
            // All string literal tokens start with quote and if it's a keyword first char is a letter (somewhat
            // loosely).
            Assertions.assertFalse(
                    tokenImage[i].charAt(0) == '"' && Character.isLetter(tokenImage[i].charAt(1)),
                    tokenImage[i] + " should be in one of the reserved word or non-reserved word group of tokens.");
        }
    }
}
