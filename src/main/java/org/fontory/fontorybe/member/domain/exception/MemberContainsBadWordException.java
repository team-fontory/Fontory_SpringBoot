package org.fontory.fontorybe.member.domain.exception;

import org.fontory.fontorybe.common.domain.SkipDiscordNotification;

@SkipDiscordNotification
public class MemberContainsBadWordException extends RuntimeException {
    public MemberContainsBadWordException() {
        super("Member contains bad word.");
    }
}
