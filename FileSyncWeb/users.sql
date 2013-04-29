-- phpMyAdmin SQL Dump
-- version 2.11.11.3
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Apr 29, 2013 at 03:12 AM
-- Server version: 5.5.29
-- PHP Version: 5.3.20

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `fileshare`
--

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE IF NOT EXISTS `users` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `password` varchar(60) NOT NULL,
  `email` text NOT NULL,
  `hash` varchar(60) NOT NULL,
  `active` int(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=7 ;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `password`, `email`, `hash`, `active`) VALUES
(1, '1658433e976885636044cc9901a37f00', 'matthew.j.joplin@vanderbilt.edu', 'afda332245e2af431fb7b672a68b659d', 1),
(2, 'e74d768497dbe99dea9bdd6a82a05415', 'sportsmatt88@yahoo.com', 'eecca5b6365d9607ee5a9d336962c534', 0),
(3, '7a4daeaee9edff6cf19ea965d6e573e7', 'jesse.l.badash@vanderbilt.edu', 'c86a7ee3d8ef0b551ed58e354a836f2b', 1),
(4, '68e109f0f40ca72a15e05cc22786f8e6', 'krzysztof.k.zienkiewicz@gmail.com', 'b5b41fac0361d157d9673ecb926af5ae', 1),
(5, '0efb2f6f3741145ffcb81ef7cb581842', 'megan.e.covington@vanderbilt.edu', 'b1a59b315fc9a3002ce38bbe070ec3f5', 1),
(6, '5f4dcc3b5aa765d61d8327deb882cf99', 'jesse.l.badash@vanderbilt.edu', 'ed265bc903a5a097f61d3ec064d96d2e', 1);
