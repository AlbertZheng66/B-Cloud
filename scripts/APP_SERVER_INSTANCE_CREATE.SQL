
/* 记录启动的应用服务器的实例 */
CREATE TABLE APP_SERVER_INSTANCE(
    OID                 VARCHAR(64)   NOT NULL,  -- 内部编码
    TEMPLATE_OID        VARCHAR(64)   NOT NULL,  -- 使用的模板ID，关联AppServerTemplate的OID
    PHY_SERVER_OID      VARCHAR(64)   NOT NULL,  -- 物理服务器ID，关联PhyServer的OID
    NAME                VARCHAR(256),            -- 应用服务器的名称(Tomcat, Jetty)
    SERVER_TYPE         VARCHAR(64),             -- 服务器类型
    VERSION             VARCHAR(256),            -- 版本号
    WORK_PATH           VARCHAR(4000),           -- 工作路径
    STARTUP_CMD         VARCHAR(512),            -- 启动命令
    STOP_CMD            VARCHAR(512),            -- 停止命令
    KILL_CMD            VARCHAR(512),            -- 强行停止命令
    STARTUP_TIME        TIMESTAMP,               -- 启动时间
    SHUTDOWN_TIME       TIMESTAMP,               -- 关闭时间
    VALID               VARCHAR(2),              -- 是否可用, Y/N
    STATE               VARCHAR(2),              -- 当前状态
    INSERT_TIME         TIMESTAMP,               -- 创建时间
    INVALID_TIME        TIMESTAMP,               -- 失效时间
    CONSTRAINT PK_APP_SERVER_INSTANCE   PRIMARY KEY (OID)
)
;
