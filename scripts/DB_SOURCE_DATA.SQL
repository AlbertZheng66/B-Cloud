INSERT INTO DB_SOURCE(OID, GROUP_OID, NAME,     IP,          PORT,   DESCRIPTION, VALID, MASTER, STATE, CREATE_TIME)
            VALUES   ('0', '0001',    'DEMO-M', '127.0.0.1', '3306', 'MASTER DB', 'Y',   'Y',    'v',   now());
			
			
INSERT INTO DB_SOURCE(OID, GROUP_OID, NAME,     IP,          PORT,   DESCRIPTION, VALID, MASTER, STATE, CREATE_TIME)
            VALUES   ('1', '0001',    'DEMO-B', '127.0.0.1', '3307', 'SLAVE DB',  'Y',   'N',    'v',   now());
