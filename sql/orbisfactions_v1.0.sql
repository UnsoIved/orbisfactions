SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;


CREATE TABLE `factions` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `description` varchar(30) NOT NULL,
  `bank` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `faction_chunks` (
  `id` int(11) NOT NULL,
  `world` varchar(50) NOT NULL,
  `x` int(11) NOT NULL,
  `z` int(11) NOT NULL,
  `faction_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `faction_roles` (
  `id` int(11) NOT NULL,
  `player_id` int(11) NOT NULL,
  `faction_id` int(11) NOT NULL,
  `role` tinyint(4) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `faction_wars` (
  `id` int(11) NOT NULL,
  `faction_a_id` int(11) NOT NULL,
  `faction_b_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `players` (
  `id` int(11) NOT NULL,
  `uuid` varchar(36) NOT NULL,
  `username` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


ALTER TABLE `factions`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

ALTER TABLE `faction_chunks`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `world` (`world`,`x`,`z`),
  ADD KEY `faction_id` (`faction_id`);

ALTER TABLE `faction_roles`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `player_id` (`player_id`,`faction_id`),
  ADD KEY `faction_id` (`faction_id`);

ALTER TABLE `faction_wars`
  ADD PRIMARY KEY (`id`),
  ADD KEY `faction_wars_ibfk_1` (`faction_a_id`),
  ADD KEY `faction_wars_ibfk_2` (`faction_b_id`);

ALTER TABLE `players`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uuid` (`uuid`);


ALTER TABLE `factions`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE `faction_chunks`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE `faction_roles`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE `faction_wars`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE `players`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;


ALTER TABLE `faction_chunks`
  ADD CONSTRAINT `faction_chunks_ibfk_1` FOREIGN KEY (`faction_id`) REFERENCES `factions` (`id`) ON DELETE CASCADE;

ALTER TABLE `faction_roles`
  ADD CONSTRAINT `faction_roles_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `faction_roles_ibfk_2` FOREIGN KEY (`faction_id`) REFERENCES `factions` (`id`) ON DELETE CASCADE;

ALTER TABLE `faction_wars`
  ADD CONSTRAINT `faction_wars_ibfk_1` FOREIGN KEY (`faction_a_id`) REFERENCES `factions` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `faction_wars_ibfk_2` FOREIGN KEY (`faction_b_id`) REFERENCES `factions` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
