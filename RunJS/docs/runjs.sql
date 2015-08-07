/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50614
 Source Host           : localhost
 Source Database       : runjs

 Target Server Type    : MySQL
 Target Server Version : 50614
 File Encoding         : utf-8

 Date: 06/17/2014 07:52:18 AM
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `osc_catalogs`
-- ----------------------------
DROP TABLE IF EXISTS `osc_catalogs`;
CREATE TABLE `osc_catalogs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `parent` int(11) DEFAULT NULL,
  `user` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1053 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `osc_codes`
-- ----------------------------
DROP TABLE IF EXISTS `osc_codes`;
CREATE TABLE `osc_codes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` int(11) NOT NULL,
  `project` int(11) NOT NULL,
  `fork` int(11) NOT NULL,
  `num` int(11) NOT NULL,
  `html` mediumtext,
  `js` mediumtext,
  `css` mediumtext,
  `status` tinyint(4) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `name` varchar(32) DEFAULT NULL,
  `ident` varchar(32) DEFAULT NULL,
  `description` mediumtext,
  `post_time` timestamp NULL DEFAULT NULL,
  `sign` varchar(32) DEFAULT NULL,
  `view_count` int(11) DEFAULT '0',
  `catalog` int(11) DEFAULT '0',
  `code_type` int(11) DEFAULT '111',
  PRIMARY KEY (`id`),
  KEY `idx_code_project` (`project`) USING BTREE,
  KEY `idx_code_ident` (`ident`) USING BTREE,
  KEY `idx_code_fork` (`fork`) USING BTREE,
  KEY `idx_code_catalog` (`catalog`) USING BTREE,
  KEY `idx_code_user` (`user`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=71387 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `osc_comments`
-- ----------------------------
DROP TABLE IF EXISTS `osc_comments`;
CREATE TABLE `osc_comments` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` int(11) DEFAULT NULL,
  `user` int(11) DEFAULT NULL,
  `content` text,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_comment_code` (`code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4164 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `osc_dynamics`
-- ----------------------------
DROP TABLE IF EXISTS `osc_dynamics`;
CREATE TABLE `osc_dynamics` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` int(11) NOT NULL,
  `type` int(11) DEFAULT NULL,
  `refer` int(11) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_dy_user` (`user`) USING BTREE,
  KEY `idx_dy_type` (`type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=84695 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `osc_favors`
-- ----------------------------
DROP TABLE IF EXISTS `osc_favors`;
CREATE TABLE `osc_favors` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` int(11) DEFAULT NULL,
  `code` int(11) DEFAULT NULL,
  `code_ident` varchar(32) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `status` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `osc_files`
-- ----------------------------
DROP TABLE IF EXISTS `osc_files`;
CREATE TABLE `osc_files` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` int(11) NOT NULL,
  `name` varchar(32) NOT NULL,
  `path` varchar(64) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `hash` varchar(32) DEFAULT NULL,
  `ident` varchar(32) DEFAULT NULL,
  `type` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_file_user` (`user`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10910 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `osc_login_bindings`
-- ----------------------------
DROP TABLE IF EXISTS `osc_login_bindings`;
CREATE TABLE `osc_login_bindings` (
  `user` int(10) unsigned NOT NULL,
  `provider` varchar(10) NOT NULL,
  `account` varchar(64) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint(4) NOT NULL,
  PRIMARY KEY (`user`,`provider`,`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `osc_msgs`
-- ----------------------------
DROP TABLE IF EXISTS `osc_msgs`;
CREATE TABLE `osc_msgs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sender` int(11) NOT NULL,
  `receiver` int(11) NOT NULL,
  `content` varchar(256) NOT NULL,
  `status` int(11) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `refer` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_msg_receiver` (`receiver`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=53129 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `osc_plugin_codes`
-- ----------------------------
DROP TABLE IF EXISTS `osc_plugin_codes`;
CREATE TABLE `osc_plugin_codes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `codeid` int(11) DEFAULT NULL,
  `html` mediumtext,
  `js` mediumtext,
  `css` mediumtext,
  `create_time` timestamp NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_plugin_code` (`codeid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `osc_plugins`
-- ----------------------------
DROP TABLE IF EXISTS `osc_plugins`;
CREATE TABLE `osc_plugins` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `status` int(11) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `code` int(11) DEFAULT NULL,
  `user` int(11) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_plugin_user` (`user`) USING BTREE,
  KEY `idx_plugin_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1130 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `osc_projects`
-- ----------------------------
DROP TABLE IF EXISTS `osc_projects`;
CREATE TABLE `osc_projects` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` int(11) NOT NULL,
  `name` varchar(32) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `version` int(11) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_project_user` (`user`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=70858 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `osc_settings`
-- ----------------------------
DROP TABLE IF EXISTS `osc_settings`;
CREATE TABLE `osc_settings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` int(11) NOT NULL,
  `name` varchar(32) DEFAULT NULL,
  `value` varchar(1024) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_setting_user` (`user`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=6060 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `osc_square_codes`
-- ----------------------------
DROP TABLE IF EXISTS `osc_square_codes`;
CREATE TABLE `osc_square_codes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `codeid` int(11) DEFAULT NULL,
  `html` mediumtext,
  `js` mediumtext,
  `css` mediumtext,
  `create_time` timestamp NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `code_type` int(11) DEFAULT '111',
  PRIMARY KEY (`id`),
  KEY `idx_square_code` (`codeid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=472 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `osc_users`
-- ----------------------------
DROP TABLE IF EXISTS `osc_users`;
CREATE TABLE `osc_users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ident` varchar(64) NOT NULL,
  `type` varchar(32) NOT NULL,
  `name` varchar(32) NOT NULL,
  `email` varchar(64) DEFAULT NULL,
  `email_validated` tinyint(4) NOT NULL,
  `status` tinyint(4) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `online` tinyint(4) DEFAULT NULL,
  `role` tinyint(4) DEFAULT NULL,
  `portrait` varchar(64) DEFAULT NULL,
  `account` varchar(64) DEFAULT NULL,
  `space_url` varchar(64) DEFAULT NULL,
  `blog` varchar(64) DEFAULT NULL,
  `validated_id` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_email` (`email`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=27794 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `osc_visit_stats`
-- ----------------------------
DROP TABLE IF EXISTS `osc_visit_stats`;
CREATE TABLE `osc_visit_stats` (
  `stat_date` int(10) unsigned NOT NULL,
  `type` tinyint(4) NOT NULL,
  `id` int(10) unsigned NOT NULL,
  `view_count` int(11) NOT NULL,
  PRIMARY KEY (`stat_date`,`type`,`id`),
  KEY `idx_visit_stat_id` (`id`) USING BTREE,
  KEY `idx_visit_stat_date` (`stat_date`) USING BTREE,
  KEY `idx_visit_stat_type` (`type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `osc_votes`
-- ----------------------------
DROP TABLE IF EXISTS `osc_votes`;
CREATE TABLE `osc_votes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` int(11) DEFAULT NULL,
  `value` tinyint(4) DEFAULT NULL,
  `code` int(11) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_vote_code` (`code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5764 DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS = 1;
