-- ============================================================
-- 健身打卡微信小程序 数据库建表脚本 (MySQL 8.0)
-- 编码: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS fitness_checkin
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE fitness_checkin;

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `openid`         VARCHAR(64)  NOT NULL COMMENT '微信 openid',
    `nickname`       VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
    `avatar`         VARCHAR(512) DEFAULT NULL COMMENT '头像 URL',
    `gender`         TINYINT      DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    `current_streak` INT          NOT NULL DEFAULT 0 COMMENT '当前连续打卡天数',
    `max_streak`     INT          NOT NULL DEFAULT 0 COMMENT '历史最长连续天数',
    `total_days`     INT          NOT NULL DEFAULT 0 COMMENT '累计打卡天数',
    `last_checkin_date` DATE      DEFAULT NULL COMMENT '最近一次打卡日期',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 运动类型字典表
-- ----------------------------
DROP TABLE IF EXISTS `sport_type`;
CREATE TABLE `sport_type` (
    `id`        BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`      VARCHAR(32) NOT NULL COMMENT '运动名称',
    `icon`      VARCHAR(64) DEFAULT NULL COMMENT '图标(emoji 或图标名)',
    `sort`      INT         NOT NULL DEFAULT 0 COMMENT '排序',
    `enabled`   TINYINT     NOT NULL DEFAULT 1 COMMENT '是否启用',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动类型字典';

-- ----------------------------
-- 打卡记录表
-- ----------------------------
DROP TABLE IF EXISTS `checkin`;
CREATE TABLE `checkin` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`       BIGINT       NOT NULL COMMENT '用户ID',
    `checkin_date`  DATE         NOT NULL COMMENT '打卡日期',
    `sport_type_id` BIGINT       DEFAULT NULL COMMENT '运动类型ID',
    `sport_name`    VARCHAR(32)  DEFAULT NULL COMMENT '运动名称(冗余)',
    `duration`      INT          NOT NULL DEFAULT 0 COMMENT '运动时长(分钟)',
    `calorie`       INT          NOT NULL DEFAULT 0 COMMENT '消耗卡路里(估算)',
    `remark`        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `like_count`    INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_date` (`user_id`, `checkin_date`),
    KEY `idx_date` (`checkin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录表';

-- ----------------------------
-- 打卡图片表
-- ----------------------------
DROP TABLE IF EXISTS `checkin_image`;
CREATE TABLE `checkin_image` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `checkin_id` BIGINT       NOT NULL COMMENT '打卡记录ID',
    `url`        VARCHAR(512) NOT NULL COMMENT '图片URL',
    `sort`       INT          NOT NULL DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (`id`),
    KEY `idx_checkin` (`checkin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡图片表';

-- ----------------------------
-- 用户目标表
-- ----------------------------
DROP TABLE IF EXISTS `goal`;
CREATE TABLE `goal` (
    `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`       BIGINT   NOT NULL COMMENT '用户ID',
    `weekly_days`   INT      NOT NULL DEFAULT 5 COMMENT '每周目标打卡天数',
    `weekly_minutes` INT     NOT NULL DEFAULT 150 COMMENT '每周目标运动分钟',
    `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户目标表';

-- ----------------------------
-- 点赞表
-- ----------------------------
DROP TABLE IF EXISTS `like_record`;
CREATE TABLE `like_record` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `checkin_id` BIGINT   NOT NULL COMMENT '打卡记录ID',
    `user_id`    BIGINT   NOT NULL COMMENT '点赞用户ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_checkin_user` (`checkin_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- ----------------------------
-- 初始化运动类型字典
-- ----------------------------
INSERT INTO `sport_type` (`name`, `icon`, `sort`) VALUES
('跑步', '🏃', 1),
('健身', '🏋️', 2),
('瑜伽', '🧘', 3),
('骑行', '🚴', 4),
('游泳', '🏊', 5),
('球类', '⚽', 6),
('徒步', '🥾', 7),
('其他', '💪', 99);
