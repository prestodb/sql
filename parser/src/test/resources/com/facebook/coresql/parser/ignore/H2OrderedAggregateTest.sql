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

-- LISTAGG 1
SELECT LISTAGG(NAME, ', ') WITHIN GROUP (ORDER BY ID)
;


-- LISTAGG 2
SELECT LISTAGG(COALESCE(NAME, 'null'), ', ') WITHIN GROUP (ORDER BY ID)
;


-- LISTAGG 3
/* Unsupported: SELECT LISTAGG(ID, ', ') WITHIN GROUP (ORDER BY ID) OVER (ORDER BY ID); */
SELECT 1
FROM dual
;

-- ARRAY_AGG 1
SELECT Array_Agg( name )
;


-- ARRAY_AGG 2
SELECT ARRAY_AGG(NAME ORDER BY ID) FILTER (WHERE NAME IS NOT NULL)
;


-- ARRAY_AGG 3
/* SELECT ARRAY_AGG(ID ORDER BY ID) OVER (ORDER BY ID); */
SELECT 1
FROM dual
;