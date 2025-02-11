package org.fontory.fontorybe.member.domain.exception;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException() {
        super("Member not found");
    }
}
