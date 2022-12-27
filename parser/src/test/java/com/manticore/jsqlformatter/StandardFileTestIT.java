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
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * This Class is used for Fail Safe Integration Tests only
 * It will test Real Life SQLs not supported by the Parser yet
 */
public class StandardFileTestIT
        extends StandardFileTest
{
    public static final String TEST_FOLDER_STR = "src/test/resources/com/manticore/jsqlformatter/standardIT";

    public static Stream<Map.Entry<SQLKeyEntry, String>> getSqlMap()
    {
        LinkedHashMap<SQLKeyEntry, String> sqlMap = new LinkedHashMap<>();
        for (File file : new File(TEST_FOLDER_STR).listFiles(FILENAME_FILTER)) {
            try (FileReader fileReader = new FileReader(file); BufferedReader bufferedReader = new BufferedReader(
                    fileReader)) {
                extractIntoMap(sqlMap, file, bufferedReader);
            }
            catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Failed to read " + file.getAbsolutePath(), ex);
            }
        }
        return sqlMap.entrySet().stream();
    }
}
