
/* 应用可服务的地址实例 */
CREATE TABLE APP_HOST(
    OID                 VARCHAR(64)    NOT NULL,   -- 内部编码
    APP_ID              VARCHAR(64)    NOT NULL,   -- 应用编码
    HOST                VARCHAR(1024)  NOT NULL,   -- 主机地址（如果以斜线开头，表示只服务于一定的上下文）
    CONSTRAINT PK_APP_HOST   PRIMARY KEY (OID)
)
;
