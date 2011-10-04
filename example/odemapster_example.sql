-- phpMyAdmin SQL Dump
-- version 3.3.9
-- http://www.phpmyadmin.net
--
-- Servidor: localhost
-- Tiempo de generación: 04-10-2011 a las 11:02:49
-- Versión del servidor: 5.5.8
-- Versión de PHP: 5.3.5

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Base de datos: `odemapster_example`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `sport`
--

CREATE TABLE IF NOT EXISTS `sport` (
  `ID` int(11) NOT NULL DEFAULT '0',
  `Name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Volcar la base de datos para la tabla `sport`
--

INSERT INTO `sport` (`ID`, `Name`) VALUES
(100, 'Tennis');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `student`
--

CREATE TABLE IF NOT EXISTS `student` (
  `ID` int(11) NOT NULL DEFAULT '0',
  `Name` varchar(50) DEFAULT NULL,
  `Sport` int(11) DEFAULT NULL,
  `status` varchar(10) NOT NULL,
  `webPage` varchar(100) DEFAULT NULL,
  `phone` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `Sport` (`Sport`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Volcar la base de datos para la tabla `student`
--

INSERT INTO `student` (`ID`, `Name`, `Sport`, `status`, `webPage`, `phone`, `email`) VALUES
(1, 'Paul', 100, 'active', NULL, '777-3426', NULL),
(2, 'John', NULL, 'active', NULL, NULL, 'john@acd.edu'),
(3, 'George', NULL, 'active', 'www.george.edu', NULL, NULL),
(4, 'Ringo', NULL, 'active', 'www.starr.edu', '888-4537', 'ringo@acd.edu');

--
-- Filtros para las tablas descargadas (dump)
--

--
-- Filtros para la tabla `student`
--
ALTER TABLE `student`
  ADD CONSTRAINT `student_ibfk_1` FOREIGN KEY (`Sport`) REFERENCES `sport` (`ID`);
