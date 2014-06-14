/*
Navicat MySQL Data Transfer

Source Server         : oschina
Source Server Version : 50520
Source Host           : 192.168.1.11:3306
Source Database       : runjs

Target Server Type    : MYSQL
Target Server Version : 50520
File Encoding         : 65001

Date: 2012-10-11 09:49:30
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `osc_codes`
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
  PRIMARY KEY (`id`),
  KEY `idx_code_project` (`project`),
  KEY `idx_code_ident` (`ident`),
  KEY `idx_code_fork` (`fork`)
) ENGINE=InnoDB AUTO_INCREMENT=793 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `osc_comments`
-- ----------------------------
DROP TABLE IF EXISTS `osc_comments`;
CREATE TABLE `osc_comments` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` int(11) DEFAULT NULL,
  `user` int(11) DEFAULT NULL,
  `content` text,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_comment_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `osc_favors`
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
-- Records of osc_favors
-- ----------------------------

-- ----------------------------
-- Table structure for `osc_files`
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
  KEY `idx_file_user` (`user`)
) ENGINE=InnoDB AUTO_INCREMENT=104 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `osc_login_bindings`
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
-- Records of osc_login_bindings
-- ----------------------------

-- ----------------------------
-- Table structure for `osc_projects`
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
  KEY `idx_project_user` (`user`)
) ENGINE=InnoDB AUTO_INCREMENT=274 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `osc_settings`
-- ----------------------------
DROP TABLE IF EXISTS `osc_settings`;
CREATE TABLE `osc_settings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` int(11) NOT NULL,
  `name` varchar(32) DEFAULT NULL,
  `value` varchar(32) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_setting_user` (`user`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of osc_settings
-- ----------------------------
INSERT INTO `osc_settings` VALUES ('1', '1', 'theme', 'rubyblue', '2012-09-10 17:38:49');
INSERT INTO `osc_settings` VALUES ('2', '3', 'theme', 'night', '2012-10-10 17:00:54');
INSERT INTO `osc_settings` VALUES ('3', '4', 'theme', 'default', '2012-09-28 16:40:01');
INSERT INTO `osc_settings` VALUES ('4', '16', 'theme', 'default', '2012-09-26 13:42:46');
INSERT INTO `osc_settings` VALUES ('5', '5', 'theme', 'default', '2012-09-14 12:05:06');
INSERT INTO `osc_settings` VALUES ('6', '7', 'theme', 'eclipse', '2012-09-19 14:33:13');
INSERT INTO `osc_settings` VALUES ('8', '19', 'theme', 'night', '2012-10-08 14:17:04');
INSERT INTO `osc_settings` VALUES ('9', '33', 'theme', 'night', '2012-10-09 11:45:39');
INSERT INTO `osc_settings` VALUES ('10', '18', 'theme', 'default', '2012-10-08 18:02:58');
INSERT INTO `osc_settings` VALUES ('11', '29', 'theme', 'night', '2012-10-08 14:22:06');
INSERT INTO `osc_settings` VALUES ('12', '30', 'theme', 'default', '2012-10-08 15:33:05');
INSERT INTO `osc_settings` VALUES ('13', '25', 'theme', 'default', '2012-10-10 14:54:12');

-- ----------------------------
-- Table structure for `osc_users`
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
  PRIMARY KEY (`id`),
  KEY `idx_user_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `osc_votes`
-- ----------------------------
DROP TABLE IF EXISTS `osc_votes`;
CREATE TABLE `osc_votes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` int(11) DEFAULT NULL,
  `value` tinyint(4) DEFAULT NULL,
  `code` int(11) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_vote_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
