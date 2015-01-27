CREATE TABLE APP_HOST(
    OID                 VARCHAR(64)  NOT NULL,   -- 应用编码
    APP_OID             VARCHAR(64)  NOT NULL,   -- 应用编码
    HOST                VARCHAR(512) NOT NULL,   -- 应用服务的主机号
    VALID               VARCHAR(2),              -- 是否生效
    CONSTRAINT PK_APP_HOST   PRIMARY KEY (OID)
)
;
