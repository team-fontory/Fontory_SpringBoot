package org.fontory.fontorybe.font.service.port;

import java.util.List;
import java.util.Optional;
import org.fontory.fontorybe.font.domain.Font;

public interface FontRepository {
    Font save(Font font);
    List<Font> findAllByMemberId(Long memberId);
    Optional<Font> findById(Long id);
}
