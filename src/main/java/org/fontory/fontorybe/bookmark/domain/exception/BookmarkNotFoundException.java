package org.fontory.fontorybe.bookmark.domain.exception;

import org.fontory.fontorybe.common.domain.SkipDiscordNotification;

@SkipDiscordNotification
public class BookmarkNotFoundException extends RuntimeException {
    public BookmarkNotFoundException() {
        super("Bookmark not found.");
    }
}
