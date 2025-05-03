package org.fontory.fontorybe.member.domain.exception;

import org.fontory.fontorybe.common.domain.SkipDiscordNotification;

@SkipDiscordNotification
public class MemberAlreadyJoinedException extends RuntimeException {
    public MemberAlreadyJoinedException() {
        super("Member already joined.");
    }
}
