-- V1 이후 추가된 기능의 스키마 델타.
--   1) payment       : 결제(mock)
--   2) store_fitting  : 직원 매장 체험
--   3) users 컬럼 추가 : 마이페이지(프로필 이미지 + 알림 설정)
-- 타입/컬럼명은 Hibernate(ddl-auto=update) 생성 규칙과 동일하게 맞춰 validate를 통과시킨다.

-- 1) 결제 -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `payment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `amount` int NOT NULL,
  `approved_at` datetime(6) DEFAULT NULL,
  `method` enum('CARD','EASY_PAY','BANK_TRANSFER') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `order_id` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `payment_key` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('READY','PAID','FAILED','CANCELED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `gift_session_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_payment_order_id` (`order_id`),
  UNIQUE KEY `UK_payment_gift_session` (`gift_session_id`),
  CONSTRAINT `FK_payment_gift_session` FOREIGN KEY (`gift_session_id`) REFERENCES `gift_session` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2) 매장 체험 --------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `store_fitting` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `brand` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `customer_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reservation_number` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reserve_date` date DEFAULT NULL,
  `reserve_time` time DEFAULT NULL,
  `status` enum('WAITING','IN_PROGRESS','DONE','CANCELED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `material_feel` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `size_feel` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `storage_feel` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `wear_comfort` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `weight` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `overall_satisfaction` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `staff_memo` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `preferred_candidate_product_id` bigint DEFAULT NULL,
  `gift_session_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_store_fitting_reservation_number` (`reservation_number`),
  CONSTRAINT `FK_store_fitting_gift_session` FOREIGN KEY (`gift_session_id`) REFERENCES `gift_session` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3) 마이페이지 컬럼 (users) -------------------------------------------------
--    boolean → bit(1) NOT NULL. 기존 행이 있을 수 있으므로 기본값을 준다(validate는 기본값을 검사하지 않음).
ALTER TABLE `users`
  ADD COLUMN `profile_image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  ADD COLUMN `notify_enabled` bit(1) NOT NULL DEFAULT b'1',
  ADD COLUMN `notify_gift_progress` bit(1) NOT NULL DEFAULT b'1',
  ADD COLUMN `notify_taste_progress` bit(1) NOT NULL DEFAULT b'1';
