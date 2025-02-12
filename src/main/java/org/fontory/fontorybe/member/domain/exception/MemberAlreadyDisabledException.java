package org.fontory.fontorybe.member.domain.exception;

public class MemberAlreadyDisabledException extends RuntimeException {
    public MemberAlreadyDisabledException() {
        super("Member already disabled.");
    }
}
