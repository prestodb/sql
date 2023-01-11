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

-- BRACKETS 1
SELECT columnname
FROM [server-name\\server-instance]..schemaname.tablename
;

-- BRACKETS 2
SELECT columnname
FROM [server-name\\server-instance]..[schemaName].[table Name]
;

-- BRACKETS 3
SELECT columnname
FROM [server-name\\server-instance]..[schemaName].[table-Name]
;

-- BRACKETS 4
SELECT columnname
FROM [schemaName].[tableName]
;

-- BRACKETS 5
SELECT columnname
FROM schemaname.[tableName]
;

-- BRACKETS 6
SELECT columnname
FROM [schemaName].tablename
;

-- READ INSTRUMENT TRANSACTIONS WITH COLLATERAL ONLY
SELECT a.*
FROM [cfe].[TRANSACTION] a
    INNER JOIN cfe.instrument b
        ON a.id_instrument = b.id_instrument
WHERE a.id_instrument >= ?
    AND a.id_instrument <= ?
    AND EXISTS (    SELECT 1
                    FROM cfe.instrument_ref b
                        INNER JOIN cfe.instrument_collateral_hst c
                            ON b.id_instrument_ref = c.id_instrument_ref
                    WHERE b.id_instrument = a.id_instrument )
;
