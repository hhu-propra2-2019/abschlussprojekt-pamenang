DROP TABLE ANMELDUNG IF EXISTS;

CREATE TABLE ANMELDUNG{
    matno varchar(255) NOT NULL,
    name varchar(255) DEFAULT NULL,
    surname varchar(255) DEFAULT NULL,
    modul varchar(255) DEFAULT NULL,
    PRIMARY KEY(matno, modul)
}


DROP TABLE ZULASSUNG IF EXISTS;

CREATE TABLE ZULASSUNG{
    matno varchar(255) NOT NULL,
    name varchar(255) DEFAULT NULL,
    surname varchar(255) DEFAULT NULL,
    modul varchar(255) DEFAULT NULL,
    PRIMARY KEY(matno, modul)
}