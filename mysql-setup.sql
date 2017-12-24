CREATE DATABASE IF NOT EXISTS VX;

DROP TABLE IF EXISTS vx.product;

CREATE TABLE vx.product (
   id int NOT NULL AUTO_INCREMENT,
   abbrev varchar(5) NOT NULL,
   name varchar(50),
   PRIMARY KEY (ID)
);

INSERT INTO vx.product (abbrev, name) VALUES
 ('sd', 'FromDB: Silly Doileys')
,('tn', 'FromDB; Tidy Nappies')
;

-- Create a MySQL user for the vertx API with a password

CREATE USER IF NOT EXISTS 'vxapi'@'localhost' IDENTIFIED BY 'vee_ex_a_pee_i';

GRANT INSERT, SELECT, UPDATE ON VX.* TO 'vxapi'@'localhost';

