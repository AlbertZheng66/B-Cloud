CREATE TABLE PUBLISH_INFO(
    OID                 VARCHAR(64)  NOT NULL,   -- 内部编码
    APP_OID             VARCHAR(64)  NOT NULL,   -- 应用编码
    APP_VERSION_OID     VARCHAR(64)  NOT NULL,   -- 应用版本编码
    INITIAL_SERVERS     INTEGER,                 -- 初始启动的服务器数量
    MIN_SERVERS         INTEGER,                 -- 最小服务器数量
    MAX_SERVERS         INTEGER,                 -- 最大服务器数量
	INSERT_TIME         TIMESTAMP,               -- 创建时间
	AUTO_SCALE          VARCHAR(2),              -- 是否自动升级，Y/N
    CONSTRAINT PK_PUBLISH_INFO   PRIMARY KEY (OID)
)
;
