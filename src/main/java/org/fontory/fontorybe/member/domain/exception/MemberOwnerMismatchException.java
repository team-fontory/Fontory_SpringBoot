package org.fontory.fontorybe.member.domain.exception;

public class MemberOwnerMismatchException extends RuntimeException {
    public MemberOwnerMismatchException() {
        super("Member owner mismatch");
    }
}
