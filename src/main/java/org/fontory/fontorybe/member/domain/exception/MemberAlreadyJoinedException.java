package org.fontory.fontorybe.member.domain.exception;

public class MemberAlreadyJoinedException extends RuntimeException {
    public MemberAlreadyJoinedException() {
        super("Member already joined.");
    }
}
