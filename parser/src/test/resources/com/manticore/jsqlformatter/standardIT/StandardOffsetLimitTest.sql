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

-- SIMPLE LIMIT
SELECT p.*
FROM product p
    LEFT JOIN product_description pd
        ON ( p.product_id = pd.product_id )
    LEFT JOIN product_type_google_category ptgc
        ON ( p.product_type_id = ptgc.product_type_id )
    LEFT JOIN product_google_custom_label pgcl
        ON ( p.product_id = pgcl.product_id )
WHERE p.status = 1
    AND pd.language_id = 2
    AND p.product_id IN (   SELECT product_id
                            FROM cj_googleshopping_products )
ORDER BY    date_available DESC
            , p.purchased DESC
LIMIT 200000
;

-- LIMIT OFFSET EXPRESSIONS
SELECT p.*
FROM product p
LIMIT '200000'
OFFSET '5'
;

-- MYSQL LIMIT OFFSET EXPRESSIONS
SELECT p.*
FROM product p
LIMIT 5, 2000
;