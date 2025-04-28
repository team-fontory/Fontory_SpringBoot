package org.fontory.fontorybe.member.infrastructure.entity;

public enum MemberStatus {
    ONBOARDING,  // 회원가입 직후
    ACTIVATE,   // 프로필 정보까지 모두 채워진 정상 회원
    DEACTIVATE     // 회원탈퇴
}