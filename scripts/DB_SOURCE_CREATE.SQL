
/* 记录单一数据库配置相关的信息 */
CREATE TABLE DB_SOURCE(
    OID                 VARCHAR(64)  NOT NULL,   -- 内部编码
	GROUP_OID           VARCHAR(64)  NOT NULL,   -- 数据库组的编码
	NAME                VARCHAR(128) NOT NULL,   -- 数据库名称（单一数据库）
    IP                  VARCHAR(256) NOT NULL,   -- 数据库所在的服务器地址
    PORT                VARCHAR(16)  NOT NULL,   -- 数据库使用的端口号
    DESCRIPTION         VARCHAR(1024),           -- 描述信息
	VALID               VARCHAR(2),              -- 是否可用, Y/N
	MASTER              VARCHAR(2),              -- 主库，还是备库，Y/N
	STATE               VARCHAR(2),              -- 当前状态（在用，闲置）
	CREATE_TIME         TIMESTAMP,               -- 创建时间
    CONSTRAINT PK_APP_INSTANCE   PRIMARY KEY (OID)
)
;
