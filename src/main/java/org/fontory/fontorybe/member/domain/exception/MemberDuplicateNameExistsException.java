package org.fontory.fontorybe.member.domain.exception;

import org.fontory.fontorybe.common.domain.SkipDiscordNotification;

@SkipDiscordNotification
public class MemberDuplicateNameExistsException extends RuntimeException {
    public MemberDuplicateNameExistsException() {
        super("Duplicate name exists");
    }
}
