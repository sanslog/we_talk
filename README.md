<p>注意，我在application.yaml中用的数据库名称不是下面库的名称，请注意修改，这是个springboot+mysql+redis的理解难度偏普通的项目，如果对你的学习有帮助的话不妨给个star (^.^)，有些部分写的略微潦草或者不规范，因为我没有深入使用AI，是出于学习的目的手打的，见谅。</p>

<p>这个项目里值得说道一二的就是定时任务、redis缓存采取分布式锁（即redis缓存击穿、穿透、雪崩的知识点）、mysql外键约束即时间相关函数、jwt双token认证（结合redis缓存refreshToken）以及其它小知识点了。我没有集成oss服务，是因为我觉得做成分布式服务更好，做一个oss服务器，按照我的设想就是前端从oss服务器请求临时凭证--->前端通过凭证上传图片--->前端将图片url上传给后端，这样设计更好一点。</p>

<p>下面是mysql数据库建表语句</p>

```mysql
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

create table `like`(
    `article_id` bigint unsigned not null comment '指向的文章ID',
    `user_id` bigint unsigned not null comment '指向的用户ID',

    primary key (article_id,user_id) -- 生成唯一的有序序列
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户点赞/收藏记录表';
