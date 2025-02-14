package org.fontory.fontorybe.font.controller.port;

import org.fontory.fontorybe.font.controller.dto.FontCreateDTO;
import org.fontory.fontorybe.font.domain.Font;

public interface FontService {
    Font create(Long requestMemberId, FontCreateDTO fontCreateDTO);
}
