CREATE TABLE SERVER_INFO(
    OID                       VARCHAR(64) NOT NULL,    -- 内部编码
    ID                        VARCHAR(64) NOT NULL,    -- 自行编码
    APP_SERVER_INSTANCE_OID   VARCHAR(64),             -- 应用服务器编码
    NAME                      VARCHAR(128) NOT NULL,   -- 服务器名称
    IP                        VARCHAR(128) NOT NULL,   -- 服务器地址
    MANAGER_PORT              VARCHAR(32)  NOT NULL,   -- 管理端口号
    CONTEXT_PATH              VARCHAR(32),             -- 管理上下文
    JMX_RMI_PORT              VARCHAR(32),             -- JMX 通信用的端口号
    STATE                     VARCHAR(2),              -- 当前状态
    VALID                     VARCHAR(2),              -- 是否可用, Y/N
    INSERT_TIME               TIMESTAMP,               -- 创建时间
    INVALID_TIME              TIMESTAMP,               -- 失效时间（自动巡视程序将对服务器进行探测，如果不能响应，则将其设置为失效）
    CONSTRAINT PK_SERVER_INFO PRIMARY KEY (OID)
)
;

