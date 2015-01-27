
/* 记录一组数据库相关的信息（在外边看来是一个数据库整体） */
CREATE TABLE DB_GROUP(
    OID                 VARCHAR(64)  NOT NULL,   -- 内部编码
	ID                  VARCHAR(64)  NOT NULL,   -- 外部编码
	NAME                VARCHAR(128) NOT NULL,   -- 数据库组的名称
    DESCRIPTION         VARCHAR(1024),           -- 描述信息
	VALID               VARCHAR(2),              -- 是否可用, Y/N
	`DB_NAME`           VARCHAR(128) NOT NULL,   -- 数据库的名称
	`USER_NAME`         VARCHAR(64)NOT NULL,     -- 数据库用户（一个数据库组采用统一的用户）
	PASSWD              VARCHAR(64)  NOT NULL,   -- 数据库密码（一个数据库组采用统一的密码）
	URL                 VARCHAR(1024)NOT NULL,   -- 数据库连接地址（带有参数的可替换的字符串）
	DRIVER_CLASS        VARCHAR(1024)NOT NULL,   -- 数据库使用的驱动程序
	DB_SCHEMA           VARCHAR(128),            -- 数据库采用的模式
	STATE               VARCHAR(2),              -- 当前状态（在用，闲置）
	CREATE_TIME         TIMESTAMP,               -- 创建时间
    CONSTRAINT PK_APP_INSTANCE   PRIMARY KEY (OID)
)
;
