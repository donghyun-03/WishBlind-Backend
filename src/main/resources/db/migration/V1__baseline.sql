-- WishBlind 스키마 베이스라인.
--
-- ddl-auto=update로 만들어진 스키마를 그대로 옮긴 것이다. 이 시점 이후의 스키마
-- 변경은 반드시 V2, V3... 마이그레이션 파일로 추가한다.
--
-- 이미 ddl-auto로 스키마를 만들어 둔 로컬 DB는 baseline-on-migrate 설정 덕에
-- 이 파일을 실행하지 않고 "적용됨"으로만 기록한다. 따라서 기존 로컬 DB의
-- gift_session.user_id는 여전히 NULL 허용 상태로 남는다. 새로 만드는 DB에만
-- NOT NULL이 걸린다.

SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE `delivery` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `message` text COLLATE utf8mb4_unicode_ci,
  `method` enum('SHIP','STORE_PICKUP') COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `recipient_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reserve_date` date DEFAULT NULL,
  `reserve_time` time DEFAULT NULL,
  `gift_session_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6jskemnud3mb8eyo05du1rgkj` (`gift_session_id`),
  CONSTRAINT `FK658wdb36u5vga7tqtqhke2vmq` FOREIGN KEY (`gift_session_id`) REFERENCES `gift_session` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `gift_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `brand` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `budget_max` int DEFAULT NULL,
  `budget_min` int DEFAULT NULL,
  `category` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `known_avoid` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `known_colors` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `known_style` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `known_wear_style` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `invite_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `invite_token` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `meaning` text COLLATE utf8mb4_unicode_ci,
  `occasion` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `relationship` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('ANALYZING','COMPLETED','CREATED','FINALIZED','INVITED','PREPARING','RECOMMENDED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK72pr7rao7ewu23r2sd49x0yat` (`invite_token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `gift_session_mood` (
  `gift_session_id` bigint NOT NULL,
  `mood` enum('COMMEMORATIVE','ETC','LUXURY','PRACTICAL','SPECIAL','TOUCHING') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  KEY `FK11d7hliex3s8ew0e0f18mje9b` (`gift_session_id`),
  CONSTRAINT `FK11d7hliex3s8ew0e0f18mje9b` FOREIGN KEY (`gift_session_id`) REFERENCES `gift_session` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `llm_call_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `failure_code` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `input_tokens` bigint DEFAULT NULL,
  `latency_ms` bigint NOT NULL,
  `model` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `output_tokens` bigint DEFAULT NULL,
  `prompt_hash` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `success` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `brand` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `color` enum('ANY','BEIGE','BLACK','BROWN','COLOR_POINT','GREEN','WHITE') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `logo_level` enum('NONE','SUBTLE','VISIBLE') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `material` enum('ETC','FABRIC','LEATHER','METAL') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mood` enum('ANY','CLASSIC','GLAMOROUS','MODERN','SIMPLE','TRENDY') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `price` int DEFAULT NULL,
  `size` enum('ANY','BASIC','LONG','SMALL') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stock` int DEFAULT NULL,
  `wear_style` enum('DELICATE','MODERATE','STATEMENT') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `recipient_preference` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `avoid_etc` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `logo_visibility` enum('NONE','SUBTLE','VISIBLE') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `material` enum('ETC','FABRIC','LEATHER','METAL') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mood` enum('ANY','CLASSIC','GLAMOROUS','MODERN','SIMPLE','TRENDY') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `size` enum('ANY','BASIC','LONG','SMALL') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `wear_style` enum('DELICATE','MODERATE','STATEMENT') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `gift_session_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5xe65jeqb51hght1rfhai13yx` (`gift_session_id`),
  CONSTRAINT `FKmx1qsc4jqcqqy8hwad9usxyc3` FOREIGN KEY (`gift_session_id`) REFERENCES `gift_session` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `recipient_preference_avoid` (
  `recipient_preference_id` bigint NOT NULL,
  `avoid_factor` enum('BIG_LOGO','FLASHY_COLOR','HARD_TO_CARE','HEAVY','NONE','SMALL_STORAGE') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  KEY `FKsnp1f8x29cmb8q6rxtsrv4dgr` (`recipient_preference_id`),
  CONSTRAINT `FKsnp1f8x29cmb8q6rxtsrv4dgr` FOREIGN KEY (`recipient_preference_id`) REFERENCES `recipient_preference` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `recipient_preference_color` (
  `recipient_preference_id` bigint NOT NULL,
  `color` enum('ANY','BEIGE','BLACK','BROWN','COLOR_POINT','GREEN','WHITE') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  KEY `FK3ltves55s9ny8l124bre65nw` (`recipient_preference_id`),
  CONSTRAINT `FK3ltves55s9ny8l124bre65nw` FOREIGN KEY (`recipient_preference_id`) REFERENCES `recipient_preference` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `recommendation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `ai_comment` text COLLATE utf8mb4_unicode_ci,
  `best` bit(1) NOT NULL,
  `chosen` bit(1) NOT NULL,
  `match_rate` int NOT NULL,
  `ranking` int DEFAULT NULL,
  `color_stars` int DEFAULT NULL,
  `practicality_stars` int DEFAULT NULL,
  `style_stars` int DEFAULT NULL,
  `gift_session_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKa3seq22qww4wp0d0ixblmv03l` (`gift_session_id`),
  KEY `FKc16er5fa5umwsa66isdvqbscc` (`product_id`),
  CONSTRAINT `FKa3seq22qww4wp0d0ixblmv03l` FOREIGN KEY (`gift_session_id`) REFERENCES `gift_session` (`id`),
  CONSTRAINT `FKc16er5fa5umwsa66isdvqbscc` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `recommendation_consideration` (
  `recommendation_id` bigint NOT NULL,
  `consideration` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  KEY `FK110tc9cr7tebbvaglsl2q7uhr` (`recommendation_id`),
  CONSTRAINT `FK110tc9cr7tebbvaglsl2q7uhr` FOREIGN KEY (`recommendation_id`) REFERENCES `recommendation` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `recommendation_reason` (
  `recommendation_id` bigint NOT NULL,
  `reason` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  KEY `FK577euwrl60o5xllggaqan10fy` (`recommendation_id`),
  CONSTRAINT `FK577euwrl60o5xllggaqan10fy` FOREIGN KEY (`recommendation_id`) REFERENCES `recommendation` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `recommendation_tag` (
  `recommendation_id` bigint NOT NULL,
  `tag` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  KEY `FK8ehm4sbp3nlvdhimgbhxffe33` (`recommendation_id`),
  CONSTRAINT `FK8ehm4sbp3nlvdhimgbhxffe33` FOREIGN KEY (`recommendation_id`) REFERENCES `recommendation` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `refresh_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `expires_at` datetime(6) NOT NULL,
  `revoked_at` datetime(6) DEFAULT NULL,
  `token_hash` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKo2mlirhldriil2y7krapq4frt` (`token_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `social_accounts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `provider` enum('GOOGLE','KAKAO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `provider_user_id` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_social_provider_user` (`provider`,`provider_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `terms_agreements` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `agreed_at` datetime(6) NOT NULL,
  `terms_type` enum('MARKETING','PRIVACY','SERVICE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint NOT NULL,
  `version` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('ACTIVE','WITHDRAWN') COLLATE utf8mb4_unicode_ci NOT NULL,
  `withdrawn_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
