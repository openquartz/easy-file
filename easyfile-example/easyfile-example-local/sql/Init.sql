CREATE TABLE `test_school`
(
    `id`    int(10) NOT NULL,
    `name`  varchar(500) NOT NULL,
    `grade` varchar(255) DEFAULT '等级',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `test_student`
(
    `id`        bigint(20) NOT NULL AUTO_INCREMENT,
    `name`      varchar(50)  NOT NULL,
    `age`       int(10) NOT NULL,
    `school`    varchar(128) NOT NULL,
    `address`   varchar(512) NOT NULL,
    `school_id` int(10) NOT NULL DEFAULT '1',
    PRIMARY KEY (`id`),
    KEY         `idx_school` (`school_id`,`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1000001 DEFAULT CHARSET=utf8mb4;