package org.fontory.fontorybe.member.domain.exception;

import org.fontory.fontorybe.common.domain.SkipDiscordNotification;

@SkipDiscordNotification
public class MemberOwnerMismatchException extends RuntimeException {
    public MemberOwnerMismatchException() {
        super("Member owner mismatch");
    }
}
