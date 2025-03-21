package org.fontory.fontorybe.bookmark.domain.exception;

import org.fontory.fontorybe.common.domain.SkipDiscordNotification;

@SkipDiscordNotification
public class BookmarkAlreadyException extends RuntimeException {
    public BookmarkAlreadyException() {
        super("Bookmark already");
    }
}
