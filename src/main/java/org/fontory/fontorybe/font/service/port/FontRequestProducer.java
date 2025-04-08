package org.fontory.fontorybe.font.service.port;

import org.fontory.fontorybe.font.service.dto.FontRequestProduceDto;

public interface FontRequestProducer {
    void sendFontRequest(FontRequestProduceDto fontRequestProduceDto);
}
