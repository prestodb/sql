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

-- GROUP_CONCAT
SELECT Group_Concat(    DISTINCT Trim( Concat(  m.title, ' ', m.firstname
                                                , ' ', m.lastname ) )
                        ORDER BY p2m.manufacturer_type_id ASC
                        SEPARATOR ' ' ) AS manufacturer_name
FROM product_to_manufacturer p2m
    LEFT JOIN manufacturer m
        ON m.manufacturer_id = p2m.manufacturer_id
WHERE p2m.product_id = 574768
;

-- WITHOUT A FROM ITEM
-- @JSQLFormatter(indentWidth=2, keywordSpelling=LOWER, functionSpelling=CAMEL, objectSpelling=LOWER, separation=BEFORE)
select case
            when (  select ccp.campaign_id
                    from campaign_constraint_product ccp
                      inner join campaign_free_shipping_products_visibility cfspv
                        on cfspv.campaign_id = ccp.campaign_id
                    where ccp.product_id = 530729
                      and cfspv.status = 1
                    union
                    select cap.campaign_id
                    from campaign_action_product cap
                      inner join campaign_free_shipping_products_visibility cfspv
                        on cfspv.campaign_id = cap.campaign_id
                    where cap.product_id = 530729
                      and cfspv.status = 1 ) is not null
              then 1
            else 0
          end as is_free_shipping
;