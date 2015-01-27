CREATE TABLE APP_SERVER_TEMPLATE(
    OID                 VARCHAR(64) NOT NULL,    -- 内部编码
    NAME                VARCHAR(128) NOT NULL,   -- 应用编码
    SERVER_TYPE         VARCHAR(64) NOT NULL,    -- 服务器类型
    VERSION             VARCHAR(64) NOT NULL,    -- 版本号
    STORE_PATH          VARCHAR(512),            -- 存储位置
    PARAMS              VARCHAR(4000),           -- 参数配置(JSON 形式)
    DESCRIPTION         VARCHAR(1024),           -- 描述信息
    STARTUP_CMD         VARCHAR(512),            -- 启动命令
    STOP_CMD            VARCHAR(512),            -- 停止命令
    FILE_SIZE           INTEGER,                 -- 发布包的大小
    VALID               VARCHAR(2),              -- 是否可用, Y/N
    INSERT_TIME         TIMESTAMP,               -- 创建时间
    CONSTRAINT PK_APP_SERVER_TEMPLATE PRIMARY KEY (OID)
)
;
