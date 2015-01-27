
/* 应用缓存的相关信息 */
CREATE TABLE APP_CACHE(
    OID                     VARCHAR(64)    NOT NULL,   -- 内部编码
    APP_OID                 VARCHAR(64)    NOT NULL,   -- 应用的内部编码
    APP_VERSION_OID         VARCHAR(64)    NOT NULL,   -- 应用版本的内部编码
    OLD_VERSION_OID         VARCHAR(64),               -- 原版本的编码（通过升级的版本将填写此应用号码）
    CACHE_ID                VARCHAR(64)    NOT NULL,   -- CACHE 的编码（每个独立的版本使用同一个缓存，后升级的版本也使用单独的缓存）
	INSERT_TIME             TIMESTAMP DEFAULT NOW(),   -- 创建时间
    VALID                   VARCHAR(2),                -- 是否生效（是否需要？）
    CONSTRAINT PK_APP_UPGRADE   PRIMARY KEY (OID)
)
;

