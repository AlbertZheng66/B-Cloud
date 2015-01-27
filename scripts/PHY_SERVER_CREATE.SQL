CREATE TABLE PHY_SERVER(
    OID                 VARCHAR(64) NOT NULL,    -- 内部编码
    NAME                VARCHAR(128) NOT NULL,   -- 服务器名称
    IP                  VARCHAR(128) NOT NULL,   -- 服务器地址 
    WORK_PATH           VARCHAR(2048),           -- 工作路径
    USER_PATH           VARCHAR(2048),           -- 当前运行路径
    TEMP_PATH           VARCHAR(2048),           -- 临时目录 
    OS_NAME             VARCHAR(128),           -- 操作系统名称
    OS_VERSION          VARCHAR(128),           -- 操作系统版本
    MANAGER_PORT        VARCHAR(32)  NOT NULL,   -- 管理端口号
    STATE               VARCHAR(2),              -- 当前状态（自动巡视程序将对服务器进行探测，如果不能响应，则将其设置为“停止”）
    VALID               VARCHAR(2),              -- 是否可用, Y/N 
    LAST_UPDATED_TIME   TIMESTAMP,               -- 最后一次更新时间
    INSERT_TIME         TIMESTAMP,               -- 创建时间
    INVALID_TIME        TIMESTAMP,               -- 失效时间
    CONSTRAINT PK_PHY_SERVER PRIMARY KEY (OID)
)
;

