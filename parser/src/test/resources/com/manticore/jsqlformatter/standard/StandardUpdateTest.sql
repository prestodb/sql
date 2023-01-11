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

-- UPDATE COLLATERAL_TYPE
UPDATE common.collateral_type
SET hair_cut = least
WHERE id_collateral_type_ref IN (   SELECT id_collateral_type_ref
                                    FROM common.collateral_type a
                                    WHERE id_status IN (    'C', 'H', 'C'
                                                            , 'H', 'C', 'H'
                                                            , 'C', 'H' )
                                        AND id_collateral_type_ref = (  SELECT Max( id_collateral_type_ref )
                                                                        FROM common.collateral_type
                                                                        WHERE id_status IN ( 'C', 'H' )
                                                                            AND id_collateral_type = a.id_collateral_type ) )
;

-- UPDATE COUNTERPARTY_INSTRUMENT
UPDATE risk.counterparty_instrument a1
SET (   priority
        , type
        , description
        , limit_amout
        , id_currency
        , end_date ) = (    SELECT  a.priority
                                    , a.type
                                    , a.description
                                    , a.limit_amout
                                    , a.id_currency
                                    , a.end_date
                            FROM risk.imp_counterparty_instrument a
                                INNER JOIN risk.counterparty b
                                    ON a.id_counterparty = b.id_counterparty
                                        AND b.id_status = 'C'
                                INNER JOIN risk.instrument c
                                    ON a.id_instrument_beneficiary = c.id_instrument
                                        AND c.id_status = 'C'
                                INNER JOIN risk.counterparty_instrument e
                                    ON b.id_counterparty_ref = e.id_counterparty_ref
                                        AND e.id_instrument_beneficiary = a.id_instrument_beneficiary
                                        AND e.id_instrument_guarantee = a.id_instrument_guarantee
                            WHERE e.id_counterparty_ref = a1.id_counterparty_ref
                                AND e.id_instrument_beneficiary = a1.id_instrument_beneficiary
                                AND e.id_instrument_guarantee = a1.id_instrument_guarantee )
WHERE EXISTS (  SELECT  a.priority
                        , a.type
                        , a.description
                        , a.limit_amout
                        , a.id_currency
                        , a.end_date
                FROM risk.imp_counterparty_instrument a
                    INNER JOIN risk.counterparty b
                        ON a.id_counterparty = b.id_counterparty
                            AND b.id_status = 'C'
                    INNER JOIN risk.instrument c
                        ON a.id_instrument_beneficiary = c.id_instrument
                            AND c.id_status = 'C'
                    INNER JOIN risk.counterparty_instrument e
                        ON b.id_counterparty_ref = e.id_counterparty_ref
                            AND e.id_instrument_beneficiary = a.id_instrument_beneficiary
                            AND e.id_instrument_guarantee = a.id_instrument_guarantee
                WHERE e.id_counterparty_ref = a1.id_counterparty_ref
                    AND e.id_instrument_beneficiary = a1.id_instrument_beneficiary
                    AND e.id_instrument_guarantee = a1.id_instrument_guarantee )
;
