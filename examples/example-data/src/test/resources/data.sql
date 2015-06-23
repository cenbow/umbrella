CREATE TABLE t_student (
    STUDENTID BIGINT IDENTITY NOT NULL, 
    STUDENTNAME VARCHAR, 
    BIRTHDAY DATE,
    TEACHERID BIGINT, 
    AGE INTEGER,
    PRIMARY KEY (STUDENTID)
);

CREATE TABLE t_teacher (
    TEACHERID BIGINT IDENTITY NOT NULL, 
    TEACHERNAME VARCHAR, 
    PRIMARY KEY (TEACHERID)
);

INSERT INTO t_teacher(TEACHERID, TEACHERNAME) VALUES(1, 'teacher1');

INSERT INTO t_teacher(TEACHERID, TEACHERNAME) VALUES(2, 'teacher2');

INSERT INTO t_student(STUDENTID, STUDENTNAME, TEACHERID) VALUES(1, 'stu1', 1);

INSERT INTO t_student(STUDENTID, STUDENTNAME, AGE, TEACHERID) VALUES(2, 'stu2', 18, 1);

INSERT INTO t_student(STUDENTID, STUDENTNAME, AGE, TEACHERID) VALUES(3, 'stu3', 20, 2);
