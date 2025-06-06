CREATE DATABASE IF NOT EXISTS FONTORY;
CREATE DATABASE IF NOT EXISTS TESTDB;

CREATE USER IF NOT EXISTS 'fontory'@'%' IDENTIFIED BY 'fontroyPW';

GRANT ALL PRIVILEGES ON FONTORY.* TO 'fontory'@'%';
GRANT ALL PRIVILEGES ON TESTDB.* TO 'fontory'@'%';

FLUSH PRIVILEGES;