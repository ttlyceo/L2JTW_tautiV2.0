CREATE TABLE IF NOT EXISTS `character_summons` (
	`ownerId` INT(10) UNSIGNED NOT NULL,
	`summonSkillId` INT(10) UNSIGNED NOT NULL,
	`curHp` INT(9) UNSIGNED NULL DEFAULT '0',
	`curMp` INT(9) UNSIGNED NULL DEFAULT '0',
	`time` INT(10) UNSIGNED NOT NULL DEFAULT '0',
	INDEX `Index 1` (`ownerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
