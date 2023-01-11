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

-- DELETE INSTRUMENT ATTRIBUTE HST AFTER VALUE_DATE_P
DELETE a FROM cfe.instrument_attribute_hst2 a
    INNER JOIN (    SELECT  value_date
                            , posting_date
                    FROM cfe.execution
                    WHERE posting_date > (  SELECT Max( posting_date )
                                            FROM cfe.execution
                                            WHERE id_status = 'R'
                                                AND value_date <= :value_date_p )
                        OR (    SELECT Max( posting_date )
                                FROM cfe.execution
                                WHERE id_status = 'R'
                                    AND value_date <= :value_date_p ) IS NULL ) b
        ON a.value_date = b.value_date
            AND b.posting_date = b.posting_date
;

-- READ INSTRUMENT TRANSACTIONS WITH COLLATERAL ONLY1
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
