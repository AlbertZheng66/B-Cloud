
/* 记录和缓存相关的参数 */
CREATE TABLE EHCACHE_PARAMETER(
    OID                 VARCHAR(64)  NOT NULL,   -- 内部编码
    CACHE_ID            VARCHAR(64)  NOT NULL,   -- 应用编码
    APP_INSTANCE_OID    VARCHAR(64),             -- 应用实例的内部编码
    BIND_ADDR           VARCHAR(128),            -- 组通信的绑定地址
    BIND_PORT           VARCHAR(32),             -- 组通信占用的端口
	INSERT_TIME         TIMESTAMP DEFAULT NOW(), -- 插入时间
    CONSTRAINT PK_EHCACHE_PARAMETER   PRIMARY KEY (OID)
)
;
