
--TODO:当前应用已经发布的实例。

CREATE TABLE SERVER_INSTANCE(
    OID                 VARCHAR(64)  NOT NULL,   -- 内部编码
    SERVER_OID          VARCHAR(64)  NOT NULL,   -- 服务器编码
    CATTLE_OID          VARCHAR(64)  NOT NULL,   -- 服务实例编码（Cattle的OID）
    
    CONSTRAINT PK_SERVER_INSTANCE PRIMARY KEY (OID)
)
;

