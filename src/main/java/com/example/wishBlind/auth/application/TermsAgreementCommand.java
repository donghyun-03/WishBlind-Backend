package com.example.wishBlind.auth.application;

import com.example.wishBlind.auth.domain.TermsType;

/**
 * 약관 동의 한 건.
 *
 * @param termsType 약관 종류
 * @param version   동의한 약관 버전 (예: "1.0")
 * @param agreed    동의 여부
 */
public record TermsAgreementCommand(TermsType termsType, String version, boolean agreed) {
}
