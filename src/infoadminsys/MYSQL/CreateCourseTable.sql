/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Author:  lsh
 * Created: Apr 14, 2017
 */
USE InfoAdminSys;
CREATE TABLE course(
    id CHAR(20) NOT NULL,
    name CHAR(30) NOT NULL,
    teacher_id CHAR(20) NOT NULL,
    capacity INTEGER,
    selectedcnt INTEGER,
    status CHAR(30),
    PRIMARY KEY(id),
    FOREIGN KEY(teacher_id) REFERENCES teacher(id)
)CHARACTER SET=utf8;

ALTER TABLE course DROP FOREIGN KEY course_ibfk_1;

ALTER TABLE course ADD FOREIGN KEY(teacher_id) REFERENCES teacher(id)
ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE course CHANGE status commitStatus CHAR(30);
ALTER TABLE course ADD capacity INTEGER;
ALTER TABLE course ADD selectedcnt INTEGER;

INSERT INTO course
VALUE ('CS1001','MATLAB程序设计','123','未给分');
