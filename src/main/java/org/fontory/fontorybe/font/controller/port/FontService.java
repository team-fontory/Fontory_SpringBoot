package org.fontory.fontorybe.font.controller.port;

import java.util.List;
import org.fontory.fontorybe.file.domain.FileUploadResult;
import org.fontory.fontorybe.font.controller.dto.FontCreateDTO;
import org.fontory.fontorybe.font.controller.dto.FontDeleteResponse;
import org.fontory.fontorybe.font.controller.dto.FontDetailResponse;
import org.fontory.fontorybe.font.controller.dto.FontPageResponse;
import org.fontory.fontorybe.font.controller.dto.FontProgressResponse;
import org.fontory.fontorybe.font.controller.dto.FontProgressUpdateDTO;
import org.fontory.fontorybe.font.controller.dto.FontResponse;
import org.fontory.fontorybe.font.controller.dto.FontUpdateDTO;
import org.fontory.fontorybe.font.domain.Font;
import org.springframework.data.domain.Page;

public interface FontService {
    Font create(Long memberId, FontCreateDTO fontCreateDTO, FileUploadResult fileDetails);
    List<FontProgressResponse> getFontProgress(Long memberId);
    Font update(Long memberId, Long fontId, FontUpdateDTO fontUpdateDTO);
    Font getOrThrowById(Long id);
    Page<FontResponse> getFonts(Long memberId, int page, int size);
    FontDetailResponse getFont(Long fondId);
    FontDeleteResponse delete(Long memberId, Long fontId);
    Page<FontPageResponse> getFontPage(Long memberId, int page, int size, String sortBy, String keyword);
    List<FontResponse> getOtherFonts(Long fontId);
    List<FontResponse> getMyPopularFonts(Long memberId);
    List<FontResponse> getPopularFonts(Long memberId);
    Font updateProgress(Long fontId, FontProgressUpdateDTO fontProgressUpdateDTO);
    FontResponse fontDownload(Long memberId, Long fontId);
}
