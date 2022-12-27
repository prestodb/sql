--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

-- PREPARE TABLE
CREATE TABLE sample_data (
    "DAY"        INT
    , "VALUE"    INT
)
;

-- SIMPLE EXPRESSION LIST WITH BRACKETS
WITH sample_data ( "DAY" )
    AS ( VALUES ( 0, 1, 2 ) )
SELECT "DAY"
FROM sample_data
;

-- MULTIPLE EXPRESSION LIST WITH BRACKETS
WITH sample_data ( "DAY", "VALUE" )
    AS ( VALUES ( ( 0, 13 ), ( 1, 12 ), ( 2, 15 )
                    , ( 3, 4 ), ( 4, 8 ), ( 5, 16 ) ) )
SELECT  "DAY"
        , "VALUE"
FROM sample_data
;

-- SIMPLE EXPRESSION LIST WITHOUT BRACKETS
WITH sample_data ( "DAY" )
    AS ( VALUES 0, 1, 2 )
SELECT "DAY"
FROM sample_data
;

-- MULTIPLE EXPRESSION LIST WITHOUT BRACKETS
WITH sample_data ( "DAY", "VALUE" )
    AS ( VALUES ( 0, 13 ), ( 1, 12 ), ( 2, 15 )
                , ( 3, 4 ), ( 4, 8 ), ( 5, 16 ) )
SELECT  "DAY"
        , "VALUE"
FROM sample_data
;

-- VALUE LIST UNION SELECT
WITH split (    word
                , str
                , hascomma ) AS (
        VALUES ( '', 'Auto,A,1234444', 1 )
        UNION ALL
        SELECT  Substr( str, 0, CASE
                        WHEN Instr( str, ',' )
                            THEN Instr( str, ',' )
                        ELSE Length( str ) + 1
                    END )
                , Ltrim( Substr( str, Instr( str, ',' ) ), ',' )
                , Instr( str, ',' )
        FROM split
        WHERE hascomma )
SELECT Trim( word )
FROM split
WHERE word != ''
;