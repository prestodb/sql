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
CREATE INDEX cfe.version_info_idx1
    ON cfe.version_info(    major_version
                            , minor_version
                            , patch_level )
;

-- UNIQUE
CREATE UNIQUE INDEX cfe.interest_period_idx1
    ON cfe.interest_period( id_instrument, change_date )
;

-- MANY COLUMNS
CREATE UNIQUE INDEX cfe.version_info_idx2
    ON cfe.version_info(    major_version
                            , minor_version
                            , patch_level
                            , major_version
                            , minor_version
                            , patch_level )
;

-- MANY COLUMNS WITH TAIL OPTIONS
CREATE UNIQUE INDEX cfe.version_info_idx2
    ON cfe.version_info(    major_version
                            , minor_version
                            , patch_level
                            , major_version
                            , minor_version
                            , patch_level ) PARALLEL COMPRESS NOLOGGING
;


-- Z MANY COLUMNS WITH TAIL OPTIONS
-- @JSQLFormatter(indentWidth=2, keywordSpelling=LOWER, functionSpelling=KEEP, objectSpelling=UPPER, separation=AFTER)
create unique index CFE.VERSION_INFO_IDX2
  on CFE.VERSION_INFO(  MAJOR_VERSION,
                        MINOR_VERSION,
                        PATCH_LEVEL,
                        MAJOR_VERSION,
                        MINOR_VERSION,
                        PATCH_LEVEL ) parallel compress nologging
;
