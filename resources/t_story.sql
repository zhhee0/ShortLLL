-- 故事表（非分片，直接建在 link 数据库中）
CREATE TABLE `t_story` (
  `id`          bigint(20)    NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title`       varchar(32)   NOT NULL COMMENT '故事标题',
  `content`     varchar(1200) NOT NULL COMMENT '故事内容',
  `username`    varchar(256)  DEFAULT NULL COMMENT '发布用户名',
  `like_count`  int(11)       NOT NULL DEFAULT 0 COMMENT '点赞数',
  `create_time` datetime      DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime      DEFAULT NULL COMMENT '修改时间',
  `del_flag`    tinyint(1)    DEFAULT 0 COMMENT '删除标识 0:未删除 1:已删除',
  PRIMARY KEY (`id`),
  KEY `idx_username` (`username`),
  KEY `idx_like_create` (`like_count` DESC, `create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='故事表';

-- story like table
CREATE TABLE `t_story_like` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `story_id` bigint(20) NOT NULL COMMENT 'Story ID',
  `username` varchar(256) NOT NULL COMMENT 'Username',
  `create_time` datetime DEFAULT NULL COMMENT 'Create time',
  `update_time` datetime DEFAULT NULL COMMENT 'Update time',
  `del_flag` tinyint(1) DEFAULT 0 COMMENT 'Delete flag 0:active 1:deleted',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_story_user` (`story_id`, `username`),
  KEY `idx_story_id` (`story_id`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='story like';
