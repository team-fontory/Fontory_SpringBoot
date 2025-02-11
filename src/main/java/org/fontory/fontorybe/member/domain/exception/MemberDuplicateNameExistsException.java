package org.fontory.fontorybe.member.domain.exception;

public class MemberDuplicateNameExistsException extends RuntimeException {
    public MemberDuplicateNameExistsException() {
        super("Duplicate name exists");
    }
}
