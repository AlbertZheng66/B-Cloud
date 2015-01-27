CREATE TABLE APP(
    OID                 VARCHAR(64)  NOT NULL,   -- 内部编码
    ID                  VARCHAR(64)  NOT NULL,   -- 自行编码
    NAME                VARCHAR(128) NOT NULL,   -- 服务器名称
    DESCRIPTION         VARCHAR(1024),           -- 描述信息
    OWNER               VARCHAR(64),             -- 服务器的拥有者
    VERSION             VARCHAR(64),             -- 当前版本号
    TAGS                VARCHAR(1024),           -- 标签
    VERSION_OID         VARCHAR(512),            -- 当前使用的默认的发布版本
    CONTEXT_PATH        VARCHAR(512),            -- 应用的上下文（如果版本的上下文为空，则使用此作为应用的上下文）
	VALID               VARCHAR(2),              -- 是否可用, Y/N
	STATE               VARCHAR(2),              -- 应用的状态
	INSERT_TIME         TIMESTAMP,               -- 创建时间
    CONSTRAINT PK_APP   PRIMARY KEY (OID)
)
;
