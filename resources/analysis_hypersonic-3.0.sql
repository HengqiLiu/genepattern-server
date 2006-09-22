
ALTER TABLE PUBLIC.SUITE_MODULES ADD ID INT IDENTITY PRIMARY KEY;

CREATE TABLE SEQUENCE_TABLE 
(
  ID INTEGER NOT NULL, 
  NAME VARCHAR(100) NOT NULL, 
  NEXT_VALUE INTEGER NOT NULL, 
  PRIMARY KEY (ID), 
  UNIQUE (NAME), 
  UNIQUE (ID)
);



/* USER TABLE */

CREATE TABLE GP_USER
(
  USER_ID varchar(255), 
  GP_PASSWORD varchar(255), 
  EMAIL varchar(255),
  LAST_LOGIN_DATE timestamp,
  LAST_LOGIN_IP varchar(255),
  TOTAL_LOGIN_COUNT INTEGER  DEFAULT 0  NOT NULL,
  PRIMARY KEY (USER_ID)
);

CREATE TABLE GP_USER_PROP
(
  ID integer generated by default as identity (start with 1), 
  KEY varchar(255), 
  VALUE varchar(255), 
  GP_USER_ID varchar(255),
  foreign key (GP_USER_ID) references GP_USER(USER_ID),
  PRIMARY KEY (ID)
);

CREATE INDEX IDX_GP_KEY ON GP_USER_PROP (KEY);



INSERT INTO SEQUENCE_TABLE (ID, NAME, NEXT_VALUE) 
  VALUES(1, 'lsid_identifier_seq',  SELECT NEXT VALUE FOR LSID_IDENTIFIER_SEQ FROM DUAL);
  

INSERT INTO SEQUENCE_TABLE (ID, NAME, NEXT_VALUE) 
  VALUES(2, 'lsid_suite_identifier_seq',  SELECT NEXT VALUE FOR LSID_SUITE_IDENTIFIER_SEQ FROM DUAL);

update props set value='3.0' where key='schemaVersion';
  