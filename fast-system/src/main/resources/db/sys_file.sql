-- 文件存储配置表
CREATE TABLE IF NOT EXISTS sys_file_config (
    id           VARCHAR(32)  NOT NULL COMMENT '主键',
    name         VARCHAR(64)  NOT NULL COMMENT '配置名',
    type         VARCHAR(16)  NOT NULL COMMENT 'LOCAL/OSS/S3/FTP/SFTP',
    config       JSON         NOT NULL COMMENT '类型相关参数',
    url_prefix   VARCHAR(255) NOT NULL COMMENT '访问地址前缀（如 https://files.example.com）',
    is_active    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否激活，全表至多 1 行为 1',
    remark       VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_by   VARCHAR(64)  DEFAULT NULL,
    created_id   VARCHAR(32)  DEFAULT NULL,
    created_at   DATETIME     DEFAULT NULL,
    updated_by   VARCHAR(64)  DEFAULT NULL,
    updated_id   VARCHAR(32)  DEFAULT NULL,
    updated_at   DATETIME     DEFAULT NULL,
    is_deleted   TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_config_name (name, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件存储配置';

-- 文件记录表
CREATE TABLE IF NOT EXISTS sys_file (
    id             VARCHAR(32)   NOT NULL COMMENT '主键',
    original_name  VARCHAR(255)  NOT NULL COMMENT '原始文件名',
    storage_key    VARCHAR(512)  NOT NULL COMMENT '存储相对路径/objectKey',
    url            VARCHAR(1024) NOT NULL COMMENT '上传时算好的完整访问地址',
    size           BIGINT        NOT NULL COMMENT '文件字节数',
    content_type   VARCHAR(128)  DEFAULT NULL,
    ext            VARCHAR(16)   DEFAULT NULL,
    hash           VARCHAR(64)   DEFAULT NULL COMMENT 'sha256',
    storage_type   VARCHAR(16)   NOT NULL COMMENT '上传时使用的存储类型',
    config_id      VARCHAR(32)   NOT NULL COMMENT '上传时使用的配置 id',
    biz_type       VARCHAR(64)   DEFAULT NULL COMMENT '业务类型',
    biz_id         VARCHAR(64)   DEFAULT NULL COMMENT '业务 id',
    created_by     VARCHAR(64)   DEFAULT NULL,
    created_id     VARCHAR(32)   DEFAULT NULL,
    created_at     DATETIME      DEFAULT NULL,
    updated_by     VARCHAR(64)   DEFAULT NULL,
    updated_id     VARCHAR(32)   DEFAULT NULL,
    updated_at     DATETIME      DEFAULT NULL,
    is_deleted     TINYINT(1)    NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_file_biz (biz_type, biz_id),
    KEY idx_file_hash (hash),
    KEY idx_file_config (config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件记录';
