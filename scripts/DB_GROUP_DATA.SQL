INSERT INTO DB_GROUP (OID, ID, NAME, DESCRIPTION, VALID,`DB_NAME`, `USER_NAME`, PASSWD,	URL, DRIVER_CLASS, DB_SCHEMA, STATE, CREATE_TIME)
            VALUES   ('0001', '1', '演示库-1','演示数据库', 'y', 'demo1', 'demo1', 'demo1',
                                                                 			           'jdbc:mysql://${ip}:${port}/demo1',
																					         'com.mysql.jdbc.Driver',
                             																				null,     'y',    now());
