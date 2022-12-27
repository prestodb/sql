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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.stream.Stream;

public class SimpleFileTest
        extends StandardFileTest
{
    public static final String TEST_FOLDER_STR = "src/test/resources/com/manticore/jsqlformatter/simple";

    public static Stream<Entry<SQLKeyEntry, String>> getSqlMap()
    {
        LinkedHashMap<SQLKeyEntry, String> sqlMap = new LinkedHashMap<>();
        for (File file : new File(TEST_FOLDER_STR).listFiles(FILENAME_FILTER)) {
            extractIntoMap(sqlMap, file);
        }
        return sqlMap.entrySet().stream();
    }

    protected static void extractIntoMap(LinkedHashMap<SQLKeyEntry, String> sqlMap, File file)
    {
        try (FileReader fileReader = new FileReader(file); BufferedReader bufferedReader = new BufferedReader(
                fileReader)) {
            boolean start = false;
            boolean end;
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            String k = "";
            boolean afterHeader = false;
            while ((line = bufferedReader.readLine()) != null) {
                if (!afterHeader && line.startsWith("--")) {
                    continue;
                }
                else {
                    afterHeader = true;
                }
                if (!start && line.startsWith("--") && !line.startsWith("-- @")) {
                    k = line.substring(3).trim().toUpperCase();
                }
                start = start || (!line.startsWith("--") || line.startsWith("-- @")) && line.trim().length() > 0;
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
}
