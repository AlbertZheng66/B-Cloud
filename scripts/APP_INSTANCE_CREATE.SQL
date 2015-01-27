
/* ��¼������Ӧ��ʵ�� */
CREATE TABLE APP_INSTANCE(
    OID                 VARCHAR(64)  NOT NULL,   -- �ڲ�����
    APP_OID             VARCHAR(64)  NOT NULL,   -- Ӧ�ñ���
    APP_VERSION_OID     VARCHAR(64)  NOT NULL,   -- Ӧ�ð汾����
    SERVER_OID          VARCHAR(64)  NOT NULL,   -- ����������
	CATTLE_OID          VARCHAR(64)  NOT NULL,   -- ����ʵ������
    IP                  VARCHAR(128),            -- ��������ַ
    PORT                VARCHAR(32),             -- ռ�õĶ˿�
    BIND_ADDR           VARCHAR(128),            -- ��ͨ�ŵİ󶨵�ַ
    BIND_PORT           VARCHAR(32),             -- ��ͨ��ռ�õĶ˿�
    CONTEXT_PATH        VARCHAR(32),             -- ����������
    DEPLOY_PATH         VARCHAR(1024),           -- ������������ַ
	STARTUP_TIME        TIMESTAMP,               -- ����ʱ��
	SHUTDOWN_TIME       TIMESTAMP,               -- �ر�ʱ��
	VALID               VARCHAR(2),              -- �Ƿ����, Y/N
	STATE               VARCHAR(2),              -- ��ǰ״̬��R: ���У�I: ʧЧ��S��ֹͣ��
	INVALID_TIME        TIMESTAMP,               -- ʧЧʱ��
    CONSTRAINT PK_APP_INSTANCE   PRIMARY KEY (OID)
)
;