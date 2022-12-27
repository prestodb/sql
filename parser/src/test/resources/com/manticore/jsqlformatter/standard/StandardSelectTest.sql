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

-- SIMPLE
SELECT * FROM dual
;

-- APPEND COLLATERAL REF
SELECT /*+ PARALLEL */
    cfe.id_collateral_ref.nextval
    , id_collateral
FROM (  SELECT DISTINCT
            a.id_collateral
        FROM cfe.collateral a
            LEFT JOIN cfe.collateral_ref b
                ON a.id_collateral = b.id_collateral
        WHERE b.id_collateral_ref IS NULL )
;

-- APPEND COUNTER PARTY REF
SELECT /*+ PARALLEL */
    cfe.id_counter_party_ref.nextval
    , id_counter_party
FROM (  SELECT DISTINCT
            a.id_counter_party
        FROM cfe.collateral a
            LEFT JOIN cfe.counter_party_ref b
                ON a.id_counter_party = b.id_counter_party
        WHERE a.id_counter_party IS NOT NULL
            AND b.id_counter_party_ref IS NULL )
;

-- SELECT WITH COMPLEX ORDER
WITH ex AS (
        SELECT  value_date
                , posting_date
        FROM cfe.execution x
        WHERE id_status IN ( 'R', 'H' )
            AND value_date = (  SELECT Max( value_date )
                                FROM cfe.execution
                                WHERE id_status IN ( 'R', 'H' ) )
            AND posting_date = (    SELECT Max( posting_date )
                                    FROM cfe.execution
                                    WHERE id_status IN ( 'R', 'H' )
                                        AND value_date = x.value_date ) )
    , fxr AS (
        SELECT  id_currency_from
                , fxrate
        FROM common.fxrate_hst f
        WHERE f.value_date <= ( SELECT value_date
                                FROM ex )
            AND f.value_date = (    SELECT Max( value_date )
                                    FROM common.fxrate_hst
                                    WHERE id_currency_from = f.id_currency_from
                                        AND id_currency_into = f.id_currency_into )
            AND id_currency_into = 'NGN'
        UNION ALL
        SELECT  'NGN'
                , 1
        FROM dual )
    , scope AS (
        SELECT *
        FROM cfe.accounting_scope
        WHERE id_status = 'C'
            AND id_accounting_scope_code = 'INTERN' )
    , scope1 AS (
        SELECT *
        FROM cfe.accounting_scope
        WHERE id_status = 'C'
            AND id_accounting_scope_code = 'NGAAP' )
    , c AS (
        SELECT  b.code
                , Round( d.amount * fxr.fxrate, 2 ) balance_bc
        FROM scope
            INNER JOIN cfe.ledger_branch_branch b
                ON b.id_accounting_scope = scope.id_accounting_scope
            INNER JOIN cfe.ledger_account c
                ON b.code_inferior = c.code
                    AND c.id_accounting_scope_code = scope.id_accounting_scope_code
            INNER JOIN (    SELECT  id_account_credit id_account
                                    , amount
                            FROM cfe.ledger_account_entry
                                INNER JOIN ex
                                    ON ledger_account_entry.posting_date <= ex.posting_date
                            UNION ALL
                            SELECT  id_account_debit
                                    , - amount
                            FROM cfe.ledger_account_entry
                                INNER JOIN ex
                                    ON ledger_account_entry.posting_date <= ex.posting_date ) d
                ON c.id_account = d.id_account
            INNER JOIN fxr
                ON c.id_currency = fxr.id_currency_from
        GROUP BY b.code )
    , c1 AS (
        SELECT  b.code
                , Round( d.amount * fxr.fxrate, 2 ) balance_bc
        FROM scope1
            INNER JOIN cfe.ledger_branch_branch b
                ON b.id_accounting_scope = scope1.id_accounting_scope
            INNER JOIN cfe.ledger_account c
                ON b.code_inferior = c.code
                    AND c.id_accounting_scope_code = scope1.id_accounting_scope_code
            INNER JOIN (    SELECT  id_account_credit id_account
                                    , amount
                            FROM cfe.ledger_account_entry
                                INNER JOIN ex
                                    ON ledger_account_entry.posting_date <= ex.posting_date
                            UNION ALL
                            SELECT  id_account_debit
                                    , - amount
                            FROM cfe.ledger_account_entry
                                INNER JOIN ex
                                    ON ledger_account_entry.posting_date <= ex.posting_date ) d
                ON c.id_account = d.id_account
            INNER JOIN fxr
                ON c.id_currency = fxr.id_currency_from
        GROUP BY b.code )
SELECT /*+ PARALLEL */
    a.code code
    , Lpad( ' ', 4 * ( a.gl_level - 1 ), ' ' )
             || a.code format_code
    , b.description
    , c.balance_bc
    , c1.balance_bc
FROM scope
    INNER JOIN cfe.ledger_branch_branch a
        ON a.code = a.code_inferior
            AND a.id_accounting_scope = scope.id_accounting_scope
    INNER JOIN cfe.ledger_branch b
        ON a.id_accounting_scope = b.id_accounting_scope
            AND a.code = b.code
    LEFT JOIN c
        ON a.code = c.code
    LEFT OUTER JOIN c1
        ON a.code = c1.code
WHERE gl_level <= 3
    AND NOT ( c.balance_bc IS NULL
                AND c1.balance_bc IS NULL )
ORDER BY    (   SELECT code
                FROM cfe.ledger_branch_branch
                WHERE id_accounting_scope = a.id_accounting_scope
                    AND code_inferior = a.code
                    AND gl_level = 1 ) NULLS FIRST
            , ( SELECT code
                FROM cfe.ledger_branch_branch
                WHERE id_accounting_scope = a.id_accounting_scope
                    AND code_inferior = a.code
                    AND gl_level = 2 ) ASC NULLS FIRST
            , ( SELECT code
                FROM cfe.ledger_branch_branch
                WHERE id_accounting_scope = a.id_accounting_scope
                    AND code_inferior = a.code
                    AND gl_level = 3 ) DESC NULLS FIRST
            , ( SELECT code
                FROM cfe.ledger_branch_branch
                WHERE id_accounting_scope = a.id_accounting_scope
                    AND code_inferior = a.code
                    AND gl_level = 4 ) DESC
            , ( SELECT code
                FROM cfe.ledger_branch_branch
                WHERE id_accounting_scope = a.id_accounting_scope
                    AND code_inferior = a.code
                    AND gl_level = 5 ) ASC
            , ( SELECT code
                FROM cfe.ledger_branch_branch
                WHERE id_accounting_scope = a.id_accounting_scope
                    AND code_inferior = a.code
                    AND gl_level = 6 ) NULLS FIRST
            , ( SELECT code
                FROM cfe.ledger_branch_branch
                WHERE id_accounting_scope = a.id_accounting_scope
                    AND code_inferior = a.code
                    AND gl_level = 7 ) NULLS FIRST
            , code
;

-- ALL COLUMNS FROM TABLE
SELECT a.*
FROM cfe.instrument a
;

-- NESTED WITH
WITH teststmt1 AS (
        WITH teststmt2 AS (
                SELECT *
                FROM my_table2 )
        SELECT  col1
                , col2
        FROM teststmt2 )
SELECT *
FROM teststmt
;

(
    SELECT __time
    FROM traffic_protocol_stat_log
    LIMIT 1 )
UNION ALL (
    SELECT __time
    FROM traffic_protocol_stat_log
    ORDER BY __time
    LIMIT 1 )
;

-- GROUP BY
SELECT  a
        , b
        , c
        , Sum( d )
FROM t
GROUP BY    a
            , b
            , c
HAVING Sum( d ) > 0
    AND Count( * ) > 1
;
