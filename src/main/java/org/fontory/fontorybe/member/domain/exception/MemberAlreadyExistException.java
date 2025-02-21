package org.fontory.fontorybe.member.domain.exception;

public class MemberAlreadyExistException extends RuntimeException {
    public MemberAlreadyExistException() {
        super("Member already exists by provide");
    }
}
