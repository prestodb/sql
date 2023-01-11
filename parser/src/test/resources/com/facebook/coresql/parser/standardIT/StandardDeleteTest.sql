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

-- DELETE LEDGER BRANCH BALANCE
DELETE FROM cfe.ledger_branch_balance
WHERE ( value_date, posting_date ) = (  SELECT  value_date
                                                , posting_date
                                        FROM cfe.execution
                                        WHERE id_status = 'R'
                                            AND value_date = :VALUE_DATE )
;

-- DELETE WITH MANY ITEMS
DELETE FROM cfe.ledger_branch_balance
WHERE ( value_date, posting_date, something_else ) = (  SELECT  value_date
                                                                , posting_date
                                                                , something_else
                                                        FROM cfe.execution
                                                        WHERE id_status = 'R'
                                                            AND value_date = :VALUE_DATE )
;

-- DELETE WITH MORE ITEMS
DELETE FROM cfe.ledger_branch_balance
WHERE ( value_date
        , posting_date
        , something_else
        , value_date ) = (  SELECT  value_date
                                    , posting_date
                                    , something_else
                                    , value_date
                            FROM cfe.execution
                            WHERE id_status = 'R'
                                AND value_date = :VALUE_DATE )
;

-- DELETE WITH EVEN MORE ITEMS
DELETE FROM cfe.ledger_branch_balance
WHERE ( value_date, posting_date, something_else
        , value_date, posting_date, something_else ) = (    SELECT  value_date
                                                                    , posting_date
                                                                    , something_else
                                                                    , value_date
                                                                    , posting_date
                                                                    , something_else
                                                            FROM cfe.execution
                                                            WHERE id_status = 'R'
                                                                AND value_date = :VALUE_DATE )
;

-- DELETE INSTRUMENT HST AFTER VALUE_DATE_P
DELETE /*+ PARALLEL INDEX_FFS(A, INSTRUMENT_HST_IDX1) */ FROM cfe.instrument_hst a
WHERE ( value_date, posting_date ) IN ( SELECT  value_date
                                                , posting_date
                                        FROM cfe.execution
                                        WHERE posting_date > (  SELECT Max( posting_date )
                                                                FROM cfe.execution
                                                                WHERE id_status = 'R'
                                                                    AND value_date <= :value_date_p )
                                            OR (    SELECT Max( posting_date )
                                                    FROM cfe.execution
                                                    WHERE id_status = 'R'
                                                        AND value_date <= :value_date_p ) IS NULL )
;

-- DELETE REDUNDANT INSTRUMENT COLLATERAL HST 2
DELETE FROM cfe.instrument_collateral_hst t1
WHERE EXISTS (  SELECT 1
                FROM cfe.instrument_collateral a
                    INNER JOIN cfe.collateral_ref b
                        ON a.id_collateral = b.id_collateral
                    INNER JOIN cfe.instrument_ref c
                        ON a.id_instrument = c.id_instrument
                WHERE b.id_collateral_ref = t1.id_collateral_ref
                    AND c.id_instrument_ref = t1.id_instrument_ref
                    AND a.valid_date = t1.valid_date )
;

-- DELETE ACCOUNT ENTRIES AFTER VALUE_DATE_P
DELETE FROM cfe.ledger_account_entry a
WHERE posting_date IN ( SELECT posting_date
                        FROM cfe.execution
                        WHERE posting_date > (  SELECT Max( posting_date )
                                                FROM cfe.execution
                                                WHERE id_status = 'R'
                                                    AND value_date <= :value_date_p )
                            OR (    SELECT Max( posting_date )
                                    FROM cfe.execution
                                    WHERE id_status = 'R'
                                        AND value_date <= :value_date_p ) IS NULL )
    AND reversed = '0'
;
