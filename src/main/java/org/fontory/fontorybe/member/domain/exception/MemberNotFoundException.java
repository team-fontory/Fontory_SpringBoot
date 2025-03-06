package org.fontory.fontorybe.member.domain.exception;

import org.fontory.fontorybe.common.domain.SkipDiscordNotification;

@SkipDiscordNotification
public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException() {
        super("Member not found");
    }
}
