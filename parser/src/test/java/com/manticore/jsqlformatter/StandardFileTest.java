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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author Andreas Reichel <andreas@manticore-projects.com>
 */
public class StandardFileTest
{
    public static final Pattern COMMENT_PATTERN = Pattern.compile("(?:'[^']*+')|(?:\\\"[^\\\"]*+\\\")"
            + "|(^/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/\\s?\\n?|/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/|--.*?\\r?[\\n])", Pattern.DOTALL | Pattern.MULTILINE | Pattern.UNIX_LINES);

    public static final String TEST_FOLDER_STR = "src/test/resources/com/manticore/jsqlformatter/standard";

    public static final FilenameFilter FILENAME_FILTER = new FilenameFilter()
    {
        @Override
        public boolean accept(File dir, String name)
        {
            return name.toLowerCase().endsWith(".sql");
        }
    };

    public static final Logger LOGGER = Logger.getLogger(StandardFileTest.class.getName());

    public static Stream<Entry<SQLKeyEntry, String>> getSqlMap()
    {
        LinkedHashMap<SQLKeyEntry, String> sqlMap = new LinkedHashMap<>();

        for (File file : new File(TEST_FOLDER_STR).listFiles(FILENAME_FILTER)) {
            boolean start = false;
            boolean end;

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            String k = "";

            try (FileReader fileReader = new FileReader(file); BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                while ((line = bufferedReader.readLine()) != null) {
                    if (!start && line.startsWith("--") && !line.startsWith("-- @")) {
                        k = line.substring(3).trim().toUpperCase();
                    }

                    start = start
                            || (!line.startsWith("--") || line.startsWith("-- @")) && line.trim().length() > 0;
                    end = start && line.trim().endsWith(";");

                    if (start) {
                        stringBuilder.append(line).append("\n");
                    }

                    if (end) {
                        sqlMap.put(new SQLKeyEntry(file, k), stringBuilder.toString().trim());
                        stringBuilder.setLength(0);
                        start = false;
                    }
                }
            }
            catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Failed to read " + file.getAbsolutePath(), ex);
            }
        }

        return sqlMap.entrySet().stream();
    }

    public String buildSqlString(final String originalSql, boolean laxDeparsingCheck)
    {
        String sql = COMMENT_PATTERN.matcher(originalSql).replaceAll("");
        if (laxDeparsingCheck) {
            String s = sql.replaceAll("\\n\\s*;", ";").replaceAll("\\s+", " ").replaceAll("\\s*([!/,()=+\\-*|\\]<>])\\s*", "$1").toLowerCase().trim();
            return !s.endsWith(";") ? s + ";" : s;
        }
        else {
            return sql;
        }
    }

    /**
     * Test parsing the provided examples
     *
     */
    @DisplayName("Standard SQL File Test")
    @ParameterizedTest(name = "{index} {0}: {1}")
    @MethodSource("getSqlMap")
    public void testParser(Entry<SQLKeyEntry, String> entry)
    {
        String expected = entry.getValue();

        if (expected.length() <= new CommentMap(expected).getLength()) {
            LOGGER.warning("Skip empty statement, when found only comments: " + expected);
        }
        else {
            TestUtils.assertParseAndUnparse(expected);
        }
    }
}