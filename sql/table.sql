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
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='标签';

/**
  队伍表
 */
CREATE TABLE `team`
(
    `id`          bigint                                  NOT NULL AUTO_INCREMENT COMMENT 'id',
    `user_id`     bigint                                  NOT NULL COMMENT '用户id（队长id）',
    `name`        varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '队伍名称',
    `description` varchar(1024) COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '描述',
    `expire_time` datetime                                         DEFAULT NULL COMMENT '过期时间',
    `password`    varchar(512) COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '密码',
    `max_num`     int                                     NOT NULL DEFAULT '1' COMMENT '最大人数',
    `states`      int                                     NOT NULL DEFAULT '0' COMMENT '0-公开 1-私有 2-加密',
    `create_time` datetime                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`   tinyint                                 NOT NULL DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 67
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='队伍';

/**
  用户表
 */
CREATE TABLE `user`
(
    `id`             bigint                                  NOT NULL AUTO_INCREMENT COMMENT '用户id',
    `nickname`       varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户昵称',
    `login_account`  varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号',
    `profile_photo`  varchar(1024) COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '用户头像',
    `sex`            tinyint                                          DEFAULT NULL COMMENT '性别',
    `login_password` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
    `phone`          varchar(128) COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '电话',
    `email`          varchar(128) COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '邮箱',
    `states`         int                                     NOT NULL DEFAULT '0' COMMENT '0-正常 1-封号',
    `type`           int                                     NOT NULL DEFAULT '0' COMMENT '0-默认权限 1-管理员',
    `tags`           varchar(1024) COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '标签列表',
    `create_time`    datetime                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`      tinyint                                 NOT NULL DEFAULT '0' COMMENT '是否删除',
    `profile`        varchar(1024) COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '个人简介',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 15162572
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户';

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
  AUTO_INCREMENT = 140
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户队伍关系';


/**
  好友关系表
 */
CREATE TABLE `user_friend`
(
    `id`          bigint   NOT NULL AUTO_INCREMENT COMMENT 'id',
    `user_id`     bigint   not null COMMENT '用户 id',
    `friend_id`   bigint   not null COMMENT '用户 id',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`   tinyint  NOT NULL DEFAULT '0' COMMENT '是否删除 0-未删除 1-已删除',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='好友关系';

-- 帖子表
create table if not exists post
(
    id         bigint auto_increment comment 'id' primary key,
    title      varchar(512)                       null comment '标题',
    content    text                               null comment '内容',
    tags       varchar(1024)                      null comment '标签列表（json 数组）',
    thumbNum   int      default 0                 not null comment '点赞数',
    favourNum  int      default 0                 not null comment '收藏数',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
) comment '帖子' collate = utf8mb4_unicode_ci;

-- 帖子点赞表（硬删除）
create table if not exists post_thumb
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '帖子点赞';

-- 帖子收藏表（硬删除）
create table if not exists post_favour
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '帖子收藏';

-- 帖子评论表
CREATE TABLE `post_comment`
(
    `id`          bigint   NOT NULL AUTO_INCREMENT COMMENT 'id',
    `post_id`     bigint   NOT NULL COMMENT '帖子 id',
    `user_id`     bigint   NOT NULL COMMENT '评论用户 id',
    `content`     text     NOT NULL COMMENT '评论内容',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_Delete`   tinyint  NOT NULL DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 7
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='帖子评论'






