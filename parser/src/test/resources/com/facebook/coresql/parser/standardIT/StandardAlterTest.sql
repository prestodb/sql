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

-- ALTER TABLE ADD FOREIGN KEY
ALTER TABLE cfe.ledger_acc_entry_manual
    ADD FOREIGN KEY (id_manual_posting_batch)
        REFERENCES manual_posting_batch (id_manual_posting_batch)
;

-- ALTER TABLE DROP COLUMN
ALTER TABLE risk.collateral
    DROP COLUMN id_status
;

-- ORACLE DROP MULTIPLE COLUMNS
ALTER TABLE risk.collateral
    DROP (id_status, city )
;

-- ORACLE DROP MANY MULTIPLE COLUMNS
ALTER TABLE risk.collateral
    DROP (  id_status
            , city
            , column3
            , column4 )
;

-- ALTER TABLE ADD COLUMN
ALTER TABLE risk.collateral
    ADD COLUMN id_status VARCHAR (1) NULL
;

-- ALTER TABLE ALTER COLUMN
ALTER TABLE risk.collateral
    ALTER COLUMN id_status VARCHAR (1) NOT NULL
;

-- ORACLE ALTER TABLE ADD COLUMN
ALTER TABLE risk.collateral
    ADD id_status VARCHAR (1) NULL
;

-- ORACLE ALTER TABLE MODIFY
ALTER TABLE risk.collateral
    MODIFY id_status VARCHAR (1) NOT NULL
;

-- ORACLE ADD MULTIPLE COLUMNS
ALTER TABLE customers
    ADD (   customer_name    VARCHAR2 (45)
            , city           VARCHAR2 (40)   DEFAULT 'SEATTLE')
;

-- ORACLE MODIFY MULTIPLE COLUMNS
ALTER TABLE customers
    MODIFY (    customer_name    VARCHAR2 (100)  NOT NULL
                , city           VARCHAR2 (75)   DEFAULT 'SEATTLE' NOT NULL)
;

-- RENAME
ALTER TABLE departments
    RENAME COLUMN department_name TO dept_name
;
