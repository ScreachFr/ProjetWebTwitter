-- MySQL Script generated by MySQL Workbench
-- 02/15/16 01:21:35
-- Model: New Model    Version: 1.0
-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema gr3_dupas_gaspar
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema gr3_dupas_gaspar
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `gr3_dupas_gaspar` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `gr3_dupas_gaspar` ;

-- -----------------------------------------------------
-- Table `gr3_dupas_gaspar`.`users`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `gr3_dupas_gaspar`.`users` ;

CREATE TABLE IF NOT EXISTS `gr3_dupas_gaspar`.`users` (
  `idusers` INT NOT NULL AUTO_INCREMENT,
  `login` VARCHAR(45) NOT NULL,
  `password` CHAR(64) NOT NULL,
  `email` VARCHAR(45) NULL,
  `prenom` VARCHAR(45) NULL,
  `nom` VARCHAR(45) NULL,
  PRIMARY KEY (`idusers`),
  UNIQUE INDEX `login_UNIQUE` (`login` ASC),
  UNIQUE INDEX `email_UNIQUE` (`email` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `gr3_dupas_gaspar`.`sessions`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `gr3_dupas_gaspar`.`sessions` ;

CREATE TABLE IF NOT EXISTS `gr3_dupas_gaspar`.`sessions` (
  `key` CHAR(32) NOT NULL,
  `user_id` INT NOT NULL,
  `expiration` TIMESTAMP NULL,
  `root` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`key`, `user_id`),
  INDEX `session_index` (`key` ASC, `user_id` ASC, `expiration` ASC),
  INDEX `fk_sessions_users_idx` (`user_id` ASC),
  CONSTRAINT `fk_sessions_users`
    FOREIGN KEY (`user_id`)
    REFERENCES `gr3_dupas_gaspar`.`users` (`idusers`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `gr3_dupas_gaspar`.`friends`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `gr3_dupas_gaspar`.`friends` ;

CREATE TABLE IF NOT EXISTS `gr3_dupas_gaspar`.`friends` (
  `user1` INT NOT NULL,
  `user2` INT NOT NULL,
  `date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user1`, `user2`),
  INDEX `fk_friends_users2_idx` (`user2` ASC),
  CONSTRAINT `fk_friends_users1`
    FOREIGN KEY (`user1`)
    REFERENCES `gr3_dupas_gaspar`.`users` (`idusers`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_friends_users2`
    FOREIGN KEY (`user2`)
    REFERENCES `gr3_dupas_gaspar`.`users` (`idusers`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `gr3_dupas_gaspar`.`users`
-- -----------------------------------------------------
START TRANSACTION;
USE `gr3_dupas_gaspar`;
INSERT INTO `gr3_dupas_gaspar`.`users` (`idusers`, `login`, `password`, `email`, `prenom`, `nom`) VALUES (1, 'toto', 'azerty', 'toto@mail.com', 'preto', 'nomto');
INSERT INTO `gr3_dupas_gaspar`.`users` (`idusers`, `login`, `password`, `email`, `prenom`, `nom`) VALUES (2, 'tata', '123456', 'tata@mail.com', 'preta', 'nomta');
INSERT INTO `gr3_dupas_gaspar`.`users` (`idusers`, `login`, `password`, `email`, `prenom`, `nom`) VALUES (3, 'titi', 'qwerty', 'titi@mail.com', 'preti', 'nomti');
INSERT INTO `gr3_dupas_gaspar`.`users` (`idusers`, `login`, `password`, `email`, `prenom`, `nom`) VALUES (4, 'tutu', '987654', 'tutu@mail.com', 'pretu', 'nomtu');

COMMIT;


-- -----------------------------------------------------
-- Data for table `gr3_dupas_gaspar`.`sessions`
-- -----------------------------------------------------
START TRANSACTION;
USE `gr3_dupas_gaspar`;
INSERT INTO `gr3_dupas_gaspar`.`sessions` (`key`, `user_id`, `expiration`, `root`) VALUES ('keytoto', 1, NULL, 1);

COMMIT;


-- -----------------------------------------------------
-- Data for table `gr3_dupas_gaspar`.`friends`
-- -----------------------------------------------------
START TRANSACTION;
USE `gr3_dupas_gaspar`;
INSERT INTO `gr3_dupas_gaspar`.`friends` (`user1`, `user2`, `date`) VALUES (1, 2, default);
INSERT INTO `gr3_dupas_gaspar`.`friends` (`user1`, `user2`, `date`) VALUES (3, 4, default);
INSERT INTO `gr3_dupas_gaspar`.`friends` (`user1`, `user2`, `date`) VALUES (1, 3, default);
INSERT INTO `gr3_dupas_gaspar`.`friends` (`user1`, `user2`, `date`) VALUES (1, 4, default);

COMMIT;

