CREATE TABLE DB_SOURCE_INSTANCE(
    OID                 VARCHAR(64) NOT NULL,    -- �ڲ�����
    APP_OID             VARCHAR(64) NOT NULL,    -- Ӧ�ñ���
    APP_VERSION_OID     VARCHAR(64) NOT NULL,    -- �汾��
    DB_GROUP_OID        VARCHAR(128),            -- Ӧ�ô���
    DESCRIPTION         VARCHAR(1024) NOT NULL,  -- ������Ϣ
	VALID               VARCHAR(2),              -- �Ƿ����, Y/N
	STATE               VARCHAR(2),              -- Ӧ�õ�״̬
	CREATE_TIME         TIMESTAMP,               -- ����ʱ��
    CONSTRAINT PK_VERSION_APP PRIMARY KEY (OID)
)
;
