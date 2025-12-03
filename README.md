create database we_talk;

use we_talk;

-- 创建评论表
CREATE TABLE comment (
                         id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                         post_id BIGINT UNSIGNED NOT NULL,
                         user_id BIGINT UNSIGNED NULL,
                         parent_id BIGINT UNSIGNED DEFAULT NULL COMMENT '父评论ID，NULL表示一级评论',
                         content TEXT NOT NULL,
                         reply_to_user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '被回复的用户ID',
                         reply_to_comment_id BIGINT UNSIGNED DEFAULT NULL COMMENT '被回复的评论ID',
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         INDEX idx_post_parent (post_id, parent_id),
                         INDEX idx_reply_to (reply_to_comment_id)
);

-- 创建用户表
CREATE TABLE `user` (
                        `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户唯一ID',
                        `username` VARCHAR(50) NOT NULL COMMENT '用户名',
                        `nickname` VARCHAR(50) DEFAULT NULL COMMENT '用户昵称',
                        `password` VARCHAR(255) NOT NULL COMMENT '密码',
                        `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建文章表
CREATE TABLE `articles` (
                            `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '文章ID',
                            `user_id` BIGINT UNSIGNED NOT NULL COMMENT '作者用户ID',
                            `title` VARCHAR(200) NOT NULL COMMENT '文章标题',
                            `content` LONGTEXT NOT NULL COMMENT '文章内容',
                            `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图片URL',
                            `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-草稿, 1-已发布, 2-已删除',
                            `view_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '阅读次数',
                            `like_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
                            `comment_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '评论数',
                            `category_id` INT UNSIGNED DEFAULT NULL COMMENT '分类ID',
                            `tags` VARCHAR(255) DEFAULT NULL COMMENT '标签(逗号分隔)',
                            `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                            PRIMARY KEY (`id`),
                            INDEX `idx_user_id` (`user_id`),
                            INDEX `idx_status_created` (`status`, `created_at`),
                            INDEX `idx_category_status` (`category_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- 创建文章关联图片表
CREATE TABLE `article_images` (
                                  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '图片ID',
                                  `article_id` BIGINT UNSIGNED NOT NULL COMMENT '文章ID',
                                  `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
                                  `sort_order` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序字段',
                                  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

                                  PRIMARY KEY (`id`),
                                  INDEX `idx_article_id` (`article_id`),

                                  CONSTRAINT `fk_article_images_article_id`
                                      FOREIGN KEY (`article_id`) REFERENCES `articles` (`id`)
                                          ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章图片表';

start transaction;
alter table comment add constraint p_article foreign key(post_id) references articles(id) on delete cascade;

alter table comment add constraint pi_parent foreign key (parent_id) references comment(id) on delete cascade;

alter table comment add constraint cu_user foreign key (user_id) references user(id) on delete set null ;

alter table comment add constraint cr_user foreign key (reply_to_user_id) references user(id) on delete set null ;

alter table comment add constraint cr_comment foreign key (reply_to_comment_id) references comment(id) on delete set null ;
commit;
rollback;
