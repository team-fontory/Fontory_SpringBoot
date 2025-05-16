package org.fontory.fontorybe.font.controller.port;

import java.util.List;
import org.fontory.fontorybe.file.domain.FileUploadResult;
import org.fontory.fontorybe.font.controller.dto.*;
import org.fontory.fontorybe.font.domain.Font;
import org.springframework.data.domain.Page;

public interface FontService {
    Font create(Long memberId, FontCreateDTO fontCreateDTO, FileUploadResult fileDetails);
    List<FontProgressResponse> getFontProgress(Long memberId);
    Font getOrThrowById(Long id);
    Page<FontResponse> getFonts(Long memberId, int page, int size);
    FontResponse getFont(Long fondId, Long memberId);
    FontDeleteResponse delete(Long memberId, Long fontId);
    Page<FontPageResponse> getFontPage(Long memberId, int page, int size, String sortBy, String keyword);
    List<FontResponse> getOtherFonts(Long fontId);
    List<FontResponse> getMyPopularFonts(Long memberId);
    List<FontResponse> getPopularFonts(Long memberId);
    FontUpdateResponse updateProgress(Long fontId, FontProgressUpdateDTO fontProgressUpdateDTO);
    FontDownloadResponse fontDownload(Long memberId, Long fontId);
    Boolean isDuplicateNameExists(Long memberId, String fontName);
}
