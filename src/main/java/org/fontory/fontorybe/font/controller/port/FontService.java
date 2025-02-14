package org.fontory.fontorybe.font.controller.port;

import java.util.List;
import org.fontory.fontorybe.font.controller.dto.FontCreateDTO;
import org.fontory.fontorybe.font.controller.dto.FontProgressResponse;
import org.fontory.fontorybe.font.controller.dto.FontUpdateDTO;
import org.fontory.fontorybe.font.domain.Font;

public interface FontService {
    Font create(Long memberId, FontCreateDTO fontCreateDTO);
    List<FontProgressResponse> getFontProgress(Long memberId);
    Font update(Long memberId, Long fontId, FontUpdateDTO fontUpdateDTO);
    Font getOrThrowById(Long id);
}
