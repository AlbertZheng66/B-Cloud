CREATE TABLE APP_VERSION(
    OID                 VARCHAR(64) NOT NULL,    -- 内部编码
    APP_OID             VARCHAR(64) NOT NULL,    -- 应用编码
    VERSION             VARCHAR(64) NOT NULL,    -- 版本号
    CODE                VARCHAR(128),            -- 应用代码
    DESCRIPTION         VARCHAR(1024) NOT NULL,  -- 描述信息
    DEPLOY_FILE_NAME    VARCHAR(512),            -- 发布包的文件名称（此文件只是相对文件名）
    FILE_SIZE           INTEGER,                 -- 发布包的大小
    SEQ                 VARCHAR(2),              -- 发布序列
	VALID               VARCHAR(2),              -- 是否可用, Y/N
    CONTEXT_PATH        VARCHAR(512),            -- 应用的上下文（如果为空，则应用的上下文）
	STATE               VARCHAR(2),              -- 应用的状态
	INSERT_TIME         TIMESTAMP,               -- 创建时间
    CONSTRAINT PK_VERSION_APP PRIMARY KEY (OID)
)
;
