package org.fontory.fontorybe.unit.mock;

import java.util.List;
import org.fontory.fontorybe.file.domain.FileUploadResult;
import org.fontory.fontorybe.font.controller.dto.*;
import org.fontory.fontorybe.font.controller.port.FontService;
import org.fontory.fontorybe.font.domain.Font;
import org.fontory.fontorybe.font.domain.exception.FontNotFoundException;
import org.fontory.fontorybe.font.service.port.FontRepository;
import org.springframework.data.domain.Page;

public class FakeFontService implements FontService {
    private final FontRepository fontRepository;

    public FakeFontService(FontRepository fontRepository) {
        this.fontRepository = fontRepository;
    }

    @Override
    public Font create(Long memberId, FontCreateDTO fontCreateDTO, FileUploadResult fileDetails) {
        // Basic implementation for testing
        Font font = Font.builder()
                .name(fontCreateDTO.getName())
                .engName(fontCreateDTO.getEngName())
                .example(fontCreateDTO.getExample())
                .memberId(memberId)
                .downloadCount(0L)
                .bookmarkCount(0L)
                .key(fileDetails.getId().toString())
                .build();
        return fontRepository.save(font);
    }

    @Override
    public List<FontProgressResponse> getFontProgress(Long memberId) {
        // Not needed for bookmark service testing
        throw new UnsupportedOperationException("Not implemented for testing");
    }

    @Override
    public Font getOrThrowById(Long id) {
        return fontRepository.findById(id)
                .orElseThrow(FontNotFoundException::new);
    }

    @Override
    public Page<FontResponse> getFonts(Long memberId, int page, int size) {
        // Not needed for bookmark service testing
        throw new UnsupportedOperationException("Not implemented for testing");
    }

    @Override
    public FontResponse getFont(Long fondId, Long memberId) {
        // Not needed for bookmark service testing
        throw new UnsupportedOperationException("Not implemented for testing");
    }

    @Override
    public FontDeleteResponse delete(Long memberId, Long fontId) {
        // Not needed for bookmark service testing
        throw new UnsupportedOperationException("Not implemented for testing");
    }

    @Override
    public Page<FontPageResponse> getFontPage(Long memberId, int page, int size, String sortBy, String keyword) {
        // Not needed for bookmark service testing
        throw new UnsupportedOperationException("Not implemented for testing");
    }

    @Override
    public List<FontResponse> getOtherFonts(Long fontId) {
        // Not needed for bookmark service testing
        throw new UnsupportedOperationException("Not implemented for testing");
    }

    @Override
    public List<FontResponse> getMyPopularFonts(Long memberId) {
        // Not needed for bookmark service testing
        throw new UnsupportedOperationException("Not implemented for testing");
    }

    @Override
    public List<FontResponse> getPopularFonts(Long memberId) {
        // Not needed for bookmark service testing
        throw new UnsupportedOperationException("Not implemented for testing");
    }

    @Override
    public FontUpdateResponse updateProgress(Long fontId, FontProgressUpdateDTO fontProgressUpdateDTO) {
        // Not needed for bookmark service testing
        throw new UnsupportedOperationException("Not implemented for testing");
    }

    @Override
    public FontDownloadResponse fontDownload(Long memberId, Long fontId) {
        // Not needed for bookmark service testing
        throw new UnsupportedOperationException("Not implemented for testing");
    }

    @Override
    public Boolean isDuplicateNameExists(Long memberId, String fontName) {
        // Not needed for bookmark service testing
        return false;
    }
}