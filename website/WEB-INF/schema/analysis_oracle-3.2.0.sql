-- add tables for Group Permissions.
-- Permission Flags
CREATE TABLE PERMISSION_FLAG
(
  ID NUMBER (10, 0) NOT NULL,
  NAME VARCHAR2(32) NOT NULL
, CONSTRAINT PERMISSION_FLAG_PK PRIMARY KEY
  (
    ID
  )
  ENABLE
)
;
--Populate Permission Flags
INSERT INTO PERMISSION_FLAG (ID, NAME) VALUES ('1', 'READ_WRITE');
INSERT INTO PERMISSION_FLAG (ID, NAME) VALUES ('2', 'READ');

-- Table for storing group permissions per job
CREATE TABLE JOB_GROUP
(
  JOB_NO NUMBER (10, 0) NOT NULL,
  GROUP_ID VARCHAR2(128) NOT NULL,
  PERMISSION_FLAG NUMBER (10, 0) NOT NULL
, CONSTRAINT JG_PK PRIMARY KEY
  (
    JOB_NO,
    GROUP_ID
  )
  ENABLE
)
;

ALTER TABLE JOB_GROUP
ADD CONSTRAINT PF_FK FOREIGN KEY
(
  PERMISSION_FLAG
)
REFERENCES PERMISSION_FLAG
(
ID
) ENABLE
;

ALTER TABLE JOB_GROUP
ADD CONSTRAINT JN_FK FOREIGN KEY
(
  JOB_NO
)
REFERENCES ANALYSIS_JOB
(
JOB_NO
) ENABLE
;

-- update schema version
UPDATE PROPS SET VALUE = '3.2.0' where KEY = 'schemaVersion';

COMMIT;













