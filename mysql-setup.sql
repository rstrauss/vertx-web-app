CREATE DATABASE IF NOT EXISTS VX;

DROP TABLE IF EXISTS vx.product;

CREATE TABLE vx.product (
   id varchar(2) NOT NULL,
   name varchar(50)
);

INSERT INTO vx.product VALUES 
 ('sd', 'Silly Doileys')
,('tn', 'Tidy Nappies')
;

-- Create a MySQL user for the vertx API with a password

CREATE USER 'vxapi'@'localhost' IDENTIFIED BY 'vee_ex_a_pee_i';

GRANT INSERT, SELECT, UPDATE ON VX.* TO 'vxapi'@'localhost';

