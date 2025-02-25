CREATE TABLE IF NOT EXISTS `security`.`users` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(100) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `enabled` INT NOT NULL
);


CREATE TABLE IF NOT EXISTS `security`.`authorities` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(100) NOT NULL,
  `authority` VARCHAR(100) NOT NULL
);