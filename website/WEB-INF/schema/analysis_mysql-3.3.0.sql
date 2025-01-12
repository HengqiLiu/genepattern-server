drop table if exists BATCH_JOB;
drop table if exists BATCH_ANALYSIS;

create table BATCH_JOB (
    JOB_NO integer not null auto_increment unique,
    USER_ID varchar(255) references GP_USER ( USER_ID ),
    DELETED bit,
    DATE_SUBMITTED timestamp null,
    primary key (JOB_NO));

create table BATCH_ANALYSIS (
    BATCH_JOB integer not null references BATCH_JOB (JOB_NO) on delete cascade,
    ANALYSIS_JOB integer not null references ANALYSIS_JOB (JOB_NO) on delete cascade);

COMMIT;
