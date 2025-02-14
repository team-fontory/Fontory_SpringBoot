package org.fontory.fontorybe.font.service.port;

import org.fontory.fontorybe.font.domain.Font;

public interface FontRepository {
    Font save(Font font);
}
