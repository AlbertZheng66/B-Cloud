
/* 记录启动的应用实例 */
CREATE TABLE APP_INSTANCE(
    OID                 VARCHAR(64)  NOT NULL,   -- 内部编码
    APP_OID             VARCHAR(64)  NOT NULL,   -- 应用编码
    APP_VERSION_OID     VARCHAR(64)  NOT NULL,   -- 应用版本编码
    SERVER_OID          VARCHAR(64)  NOT NULL,   -- 服务器编码
	CATTLE_OID          VARCHAR(64)  NOT NULL,   -- 服务实例编码
    IP                  VARCHAR(128),            -- 服务器地址
    PORT                VARCHAR(32),             -- 占用的端口
    BIND_ADDR           VARCHAR(128),            -- 组通信的绑定地址
    BIND_PORT           VARCHAR(32),             -- 组通信占用的端口
    CONTEXT_PATH        VARCHAR(32),             -- 管理上下文
    DEPLOY_PATH         VARCHAR(1024),           -- 发布的物理地址
	STARTUP_TIME        TIMESTAMP,               -- 启动时间
	SHUTDOWN_TIME       TIMESTAMP,               -- 关闭时间
	VALID               VARCHAR(2),              -- 是否可用, Y/N
	STATE               VARCHAR(2),              -- 当前状态（R: 运行，I: 失效，S：停止）
	INVALID_TIME        TIMESTAMP,               -- 失效时间
    CONSTRAINT PK_APP_INSTANCE   PRIMARY KEY (OID)
)
;
