CREATE TABLE DB_SOURCE_INSTANCE(
    OID                 VARCHAR(64) NOT NULL,    -- 内部编码
    APP_OID             VARCHAR(64) NOT NULL,    -- 应用编码
    APP_VERSION_OID     VARCHAR(64) NOT NULL,    -- 版本号
    DB_GROUP_OID        VARCHAR(128),            -- 应用代码
    DESCRIPTION         VARCHAR(1024) NOT NULL,  -- 描述信息
	VALID               VARCHAR(2),              -- 是否可用, Y/N
	STATE               VARCHAR(2),              -- 应用的状态
	CREATE_TIME         TIMESTAMP,               -- 创建时间
    CONSTRAINT PK_VERSION_APP PRIMARY KEY (OID)
)
;

