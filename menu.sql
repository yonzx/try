-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Nov 09, 2026 at 12:15 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.1.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `karinderya`
--

-- --------------------------------------------------------

--
-- Table structure for table `menu`
--

CREATE TABLE `menu` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `category` varchar(30) NOT NULL,
  `image` varchar(100) NOT NULL,
  `price` decimal(8,2) NOT NULL DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `menu`
--

INSERT INTO `menu` (`id`, `name`, `category`, `image`, `price`) VALUES
(1, 'GINILING', 'Meat', 'giniling.jpg', 0.00),
(2, 'CHICKEN ADOBO', 'Meat', 'chicken adoboo.jpg', 0.00),
(3, 'MENUDO', 'Meat', 'menudoo.jpg', 0.00),
(4, 'BOPIS', 'Meat', 'bopiss.jpg', 0.00),
(5, 'SISIG', 'Meat', 'sisig.jpg', 0.00),
(6, 'ADOBONG KANGKONG', 'Veggies', 'adobong-kangkong.jpg', 0.00),
(7, 'CHAPSUY', 'Veggies', 'chapsuy.jpg', 0.00),
(8, 'GINISANG AMPLAYA', 'Veggies', 'ginisang ampalaya.jpg', 0.00),
(9, 'GINISANG PETCHAY', 'Veggies', 'ginisang petchay.jpg', 0.00),
(10, 'LANGKA', 'Veggies', 'langka.jpg', 0.00),
(11, 'HOTDOG & EGG', 'Fried Dish', 'hotdog and egg.jpg', 0.00),
(12, 'PRITONG TILAPIA', 'Fried Dish', 'tilapia.jpg', 0.00),
(13, 'PRITONG GALUNGGONG', 'Fried Dish', 'pritong galunggong.jpg', 0.00),
(14, 'LUMPIA', 'Fried Dish', 'lumpia.jpg', 0.00),
(15, 'SINIGANG NA BABOY', 'Soup', 'sinigang na baboy.jpg', 0.00);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `menu`
--
ALTER TABLE `menu`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `menu`
--
ALTER TABLE `menu`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
