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

-- INSERT RATIO COLLECTION RATIOS
INSERT INTO risk.counterparty_ratio
VALUES ( ?, ?, ? )
;

-- APPEND ATTRIBUTE VALUE REF
INSERT INTO cfe.attribute_value_ref
SELECT  cfe.id_attribute_value_ref.nextval
        , attribute_value
FROM (  SELECT DISTINCT
            a.attribute_value
        FROM cfe.instrument_attribute a
            LEFT JOIN cfe.attribute_value_ref b
                ON a.attribute_value = b.attribute_value
        WHERE b.attribute_value IS NULL ) a
;
