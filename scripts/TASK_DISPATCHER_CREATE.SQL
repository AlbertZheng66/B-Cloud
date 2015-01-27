CREATE TABLE TASK_DISPATCHER(
    OID                       VARCHAR(64) NOT NULL,    -- 内部编码
    ID                        VARCHAR(64) NOT NULL,    -- 自行编码
    TEMPLATE_ID               VARCHAR(64),             -- 模板编码
    NAME                      VARCHAR(128) NOT NULL,   -- 服务器名称
    IP                        VARCHAR(128) NOT NULL,   -- 服务器地址
    MANAGER_PORT              VARCHAR(32),             -- 管理端口号
    JMX_RMI_PORT              VARCHAR(32),             -- JMX 通信用的端口号
    STATE                     VARCHAR(2),              -- 当前状态
    VALID                     VARCHAR(2),              -- 是否可用, Y/N
    INSERT_TIME               TIMESTAMP,               -- 创建时间
    LAST_UPDATED_TIME         TIMESTAMP,               -- 最后一次更新时间
    INVALID_TIME              TIMESTAMP,               -- 失效时间（自动巡视程序将对服务器进行探测，如果不能响应，则将其设置为失效）
    CONSTRAINT PK_TASK_DISPATCHER PRIMARY KEY (OID)
)
;

