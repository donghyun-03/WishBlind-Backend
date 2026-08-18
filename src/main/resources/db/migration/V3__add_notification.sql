-- 인앱 알림 테이블. userId(선물하는 사람)에게 이벤트 알림이 쌓인다.
CREATE TABLE IF NOT EXISTS `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `type` enum('TASTE_SUBMITTED','RECOMMENDED','DELIVERY_STARTED','GIFT_COMPLETED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `message` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `gift_session_id` bigint DEFAULT NULL,
  `is_read` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_notification_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
