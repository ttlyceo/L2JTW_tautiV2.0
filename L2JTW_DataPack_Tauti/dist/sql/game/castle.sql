CREATE TABLE IF NOT EXISTS `castle` (
  `id` INT NOT NULL default 0,
  `name` varchar(25) NOT NULL,
  `taxPercent` INT NOT NULL default 15,
  `treasury` BIGINT NOT NULL default 0,
  `siegeDate` bigint(13) unsigned NOT NULL DEFAULT '0',
  `regTimeOver` enum('true','false') DEFAULT 'true' NOT NULL,
  `regTimeEnd` bigint(13) unsigned NOT NULL DEFAULT '0',
  `showNpcCrest` enum('true','false') DEFAULT 'false' NOT NULL,
  `bloodAlliance` int(3) NOT NULL default 0,
  PRIMARY KEY (`name`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT IGNORE INTO `castle` VALUES
(1,'Gludio',0,0,0,'true',0,'false',0),
(2,'Dion',0,0,0,'true',0,'false',0),
(3,'Giran',0,0,0,'true',0,'false',0),
(4,'Oren',0,0,0,'true',0,'false',0),
(5,'Aden',0,0,0,'true',0,'false',0),
(6,'Innadril',0,0,0,'true',0,'false',0),
(7,'Goddard',0,0,0,'true',0,'false',0),
(8,'Rune',0,0,0,'true',0,'false',0),
(9,'Schuttgart',0,0,0,'true',0,'false',0);