
/* 和组（JGroup）相关的配置信息 */
CREATE TABLE GROUP_CONF(
    OID                 VARCHAR(64)  NOT NULL,   -- 内部编码
    ENTITY_ID           VARCHAR(64)  NOT NULL,   -- 实体的标识符，用于唯一的标识一个使用Group的对象
    GROUP_ID            VARCHAR(64),             -- 组编码（表示一个唯一的 Group ）
    BIND_ADDR           VARCHAR(128),            -- 组通信的绑定地址
    BIND_PORT           VARCHAR(32),             -- 组通信占用的端口
	INSERT_TIME         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 插入时间
	LAST_UPDATE_TIME    TIMESTAMP NOT NULL DEFAULT '2000-01-01 00:00:00', -- 最后一次更新时间
    CONSTRAINT PK_GROUP_CONF   PRIMARY KEY (OID)
)
;
