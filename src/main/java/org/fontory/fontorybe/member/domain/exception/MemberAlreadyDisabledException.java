package org.fontory.fontorybe.member.domain.exception;

import org.fontory.fontorybe.common.domain.SkipDiscordNotification;

@SkipDiscordNotification
public class MemberAlreadyDisabledException extends RuntimeException {
    public MemberAlreadyDisabledException() {
        super("Member already disabled.");
    }
}
