/**
  帖子表
 */
CREATE TABLE `post`
(
    `id`          bigint   NOT NULL AUTO_INCREMENT COMMENT 'id',
    `title`       varchar(512) COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '标题',
    `content`     text COLLATE utf8mb4_unicode_ci COMMENT '内容',
    `tags`        varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签列表（json 数组）',
    `thumb_num`   int      NOT NULL                        DEFAULT '0' COMMENT '点赞数',
    `favour_num`  int      NOT NULL                        DEFAULT '0' COMMENT '收藏数',
    `user_id`     bigint   NOT NULL COMMENT '创建用户 id',
    `create_time` datetime NOT NULL                        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL                        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`   tinyint  NOT NULL                        DEFAULT '0' COMMENT '是否删除',
    `img`         varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图片',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 24
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='帖子';

/**
  帖子评论表
 */
CREATE TABLE `post_comment`
(
    `id`          bigint   NOT NULL AUTO_INCREMENT COMMENT 'id',
    `post_id`     bigint   NOT NULL COMMENT '帖子 id',
    `user_id`     bigint   NOT NULL COMMENT '评论用户 id',
    `content`     text     NOT NULL COMMENT '评论内容',
    'thumb_num'   int      NOT NULL DEFAULT '0' COMMENT '点赞数',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_Delete`   tinyint  NOT NULL DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 26
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='帖子评论';

/**
  帖子收藏表
 */
CREATE TABLE `post_favour`
(
    `id`          bigint   NOT NULL AUTO_INCREMENT COMMENT 'id',
    `post_id`     bigint   NOT NULL COMMENT '帖子 id',
    `user_id`     bigint   NOT NULL COMMENT '创建用户 id',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_userId` (`user_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 78
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='帖子收藏';

/**
  帖子点赞表
 */
CREATE TABLE `post_thumb`
(
    `id`          bigint   NOT NULL AUTO_INCREMENT COMMENT 'id',
    `post_id`     bigint   NOT NULL COMMENT '帖子 id',
    `user_id`     bigint   NOT NULL COMMENT '创建用户 id',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_userId` (`user_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 149
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='帖子点赞';


/**
  评论点赞表
 */
CREATE TABLE `comment_thumb`
(
    `id`          bigint   NOT NULL AUTO_INCREMENT COMMENT 'id',
    `comment_id`  bigint   NOT NULL COMMENT '帖子 id',
    `user_id`     bigint   NOT NULL COMMENT '创建用户 id',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_userId` (`user_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 149
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='评论点赞';

/**
  标签表
 */
CREATE TABLE `tag`
(
    `id`          bigint   NOT NULL AUTO_INCREMENT COMMENT 'id',
    `tag_name`    varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签名称',
    `user_id`     bigint                                  DEFAULT NULL COMMENT '用户 id',
    `parent_id`   bigint                                  DEFAULT NULL COMMENT '父标签 id',
    `is_parent`   tinyint                                 DEFAULT NULL COMMENT '0-不是父标签 1-父标签',
    `create_time` datetime NOT NULL                       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL                       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`   tinyint  NOT NULL                       DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `unidx_userId` (`user_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 17
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='标签';

/**
  队伍表
 */
CREATE TABLE `team`
(
    `id`            bigint                                  NOT NULL AUTO_INCREMENT COMMENT 'id',
    `user_id`       bigint                                  NOT NULL COMMENT '用户id（队长id）',
    `name`          varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '队伍名称',
    `description`   varchar(1024) COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '描述',
    `expire_time`   datetime                                         DEFAULT NULL COMMENT '过期时间',
    `password`      varchar(512) COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '密码',
    `max_num`       int                                     NOT NULL DEFAULT '1' COMMENT '最大人数',
    `states`        int                                     NOT NULL DEFAULT '0' COMMENT '0-公开 1-私有 2-加密',
    `create_time`   datetime                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`     tinyint                                 NOT NULL DEFAULT '0' COMMENT '是否删除',
    `profile_photo` varchar(1024) COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '队伍头像',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 76
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='队伍';

/**
  用户表
 */
CREATE TABLE `user`
(
    `id`             bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '用户id',
    `nickname`       varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户昵称',
    `login_account`  varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号',
    `profile_photo`  varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '用户头像',
    `sex`            tinyint                                                                DEFAULT NULL COMMENT '性别',
    `login_password` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
    `phone`          varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '电话',
    `email`          varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '邮箱',
    `states`         int                                                           NOT NULL DEFAULT '0' COMMENT '0-正常 1-封号',
    `type`           int                                                           NOT NULL DEFAULT '0' COMMENT '0-默认权限 1-管理员',
    `tags`           varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '标签列表',
    `create_time`    datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`      tinyint                                                       NOT NULL DEFAULT '0' COMMENT '是否删除',
    `profile`        varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '个人简介',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 15162595
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户';

/**
  关注关系表
 */
CREATE TABLE `user_follows`
(
    `id`          bigint   NOT NULL AUTO_INCREMENT COMMENT 'id',
    `user_id`     bigint   NOT NULL COMMENT '用户 id',
    `love_id`     bigint   NOT NULL COMMENT '用户 id',
    `is_follow`   tinyint(1)        DEFAULT NULL COMMENT '是否关注 0-未关注 1-已关注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`   tinyint  NOT NULL DEFAULT '0' COMMENT '是否删除 0-未删除 1-已删除',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 54
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='关注关系';

/**
  用户队伍关系表
 */
CREATE TABLE `user_team`
(
    `id`          bigint   NOT NULL AUTO_INCREMENT COMMENT 'id',
    `user_id`     bigint   NOT NULL COMMENT '用户id',
    `team_id`     bigint   NOT NULL COMMENT '队伍id',
    `join_time`   datetime          DEFAULT NULL COMMENT '加入时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`   tinyint  NOT NULL DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 166
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户队伍关系表';


/**
  聊天室消息表
 */
CREATE TABLE `room_message`
(
    `id`          bigint   NOT NULL AUTO_INCREMENT COMMENT 'id',
    `teamId`      bigint                                  DEFAULT NULL COMMENT '队伍id（公开房间的队伍id为null）',
    `user_id`     bigint                                  DEFAULT NULL COMMENT '用户 id',
    `user_awata`  varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户头像',
    `create_time` datetime NOT NULL                       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL                       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`   tinyint  NOT NULL                       DEFAULT '0' COMMENT '是否删除',
    `message`     varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '小心',
    `nickname`    varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户昵称',
    PRIMARY KEY (`id`),
    KEY `unidx_userId` (`user_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 108
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='聊天室消息';


/**
  用户消息表
 */
CREATE TABLE `user_message`
(
    `id`            bigint   NOT NULL AUTO_INCREMENT COMMENT 'id',
    `from_id`       bigint   NOT NULL COMMENT '发送信息用户 id',
    `to_id`         bigint   NOT NULL COMMENT '接受信息用户 id',
    `from_awata`    varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发送信息用户头像',
    `to_awata`      varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '接收信息用户头像',
    `message`       varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '信息',
    `create_time`   datetime NOT NULL                       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime NOT NULL                       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`     tinyint  NOT NULL                       DEFAULT '0' COMMENT '是否删除',
    `from_nickname` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发送信息用户昵称',
    `to_nickname`   varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '接收信息用户昵称',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 24
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户消息';










