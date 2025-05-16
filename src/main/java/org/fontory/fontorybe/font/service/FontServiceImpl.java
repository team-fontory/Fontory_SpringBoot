package org.fontory.fontorybe.font.service;

import com.vane.badwordfiltering.BadWordFiltering;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fontory.fontorybe.bookmark.service.port.BookmarkRepository;
import org.fontory.fontorybe.file.application.port.CloudStorageService;
import org.fontory.fontorybe.file.application.port.FileService;
import org.fontory.fontorybe.file.domain.FileMetadata;
import org.fontory.fontorybe.file.domain.FileUploadResult;
import org.fontory.fontorybe.font.controller.dto.FontCreateDTO;
import org.fontory.fontorybe.font.controller.dto.FontDeleteResponse;
import org.fontory.fontorybe.font.controller.dto.FontDownloadResponse;
import org.fontory.fontorybe.font.controller.dto.FontPageResponse;
import org.fontory.fontorybe.font.controller.dto.FontProgressResponse;
import org.fontory.fontorybe.font.controller.dto.FontProgressUpdateDTO;
import org.fontory.fontorybe.font.controller.dto.FontResponse;
import org.fontory.fontorybe.font.controller.dto.FontUpdateResponse;
import org.fontory.fontorybe.font.controller.port.FontService;
import org.fontory.fontorybe.font.domain.Font;
import org.fontory.fontorybe.font.domain.exception.FontContainsBadWordException;
import org.fontory.fontorybe.font.domain.exception.FontDuplicateNameExistsException;
import org.fontory.fontorybe.font.domain.exception.FontInvalidStatusException;
import org.fontory.fontorybe.font.domain.exception.FontNotFoundException;
import org.fontory.fontorybe.font.domain.exception.FontOwnerMismatchException;
import org.fontory.fontorybe.font.infrastructure.entity.FontStatus;
import org.fontory.fontorybe.font.service.dto.FontRequestProduceDto;
import org.fontory.fontorybe.font.service.port.FontRepository;
import org.fontory.fontorybe.font.service.port.FontRequestProducer;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.sms.application.port.PhoneNumberStorage;
import org.fontory.fontorybe.sms.application.port.SmsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class FontServiceImpl implements FontService {
    private final FileService fileService;
    private final FontRepository fontRepository;
    private final BookmarkRepository bookmarkRepository;
    private final MemberLookupService memberLookupService;
    private final FontRequestProducer fontRequestProducer;
    private final CloudStorageService cloudStorageService;
    private final BadWordFiltering badWordFiltering;
    private final SmsService smsService;
    private final PhoneNumberStorage phoneNumberStorage;

    @Override
    @Transactional
    public Font create(Long memberId, FontCreateDTO fontCreateDTO, FileUploadResult fileDetails) {
        log.info("Service executing: Creating font for member ID: {}, font name: {}", memberId, fontCreateDTO.getName());
        Member member = memberLookupService.getOrThrowById(memberId);

        if (isDuplicateNameExists(memberId, fontCreateDTO.getName())) {
            throw new FontDuplicateNameExistsException();
        }

        checkFontNameAndExampleContainsBadWord(fontCreateDTO.getName(), fontCreateDTO.getEngName(), fontCreateDTO.getExample());

        FileMetadata fileMetadata = fileService.getOrThrowById(fileDetails.getId());

        Font savedFont = fontRepository.save(Font.from(fontCreateDTO, member.getId(), fileMetadata.getKey()));
        String fontPaperUrl = cloudStorageService.getFontPaperUrl(savedFont.getKey());
        fontRequestProducer.sendFontRequest(FontRequestProduceDto.from(savedFont, member, fontPaperUrl));

        if (fontCreateDTO.getPhoneNumber() != null && !fontCreateDTO.getPhoneNumber().isBlank()) {
            phoneNumberStorage.savePhoneNumber(savedFont, fontCreateDTO.getPhoneNumber());
            smsService.sendFontCreationNotification(fontCreateDTO.getPhoneNumber(), fontCreateDTO.getName());
        }

        log.info("Service completed: Font created with ID: {} and Font template image uploaded successfully", savedFont.getId());
        return savedFont;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FontProgressResponse> getFontProgress(Long memberId) {
        log.info("Service executing: Fetching font progress for member ID: {}", memberId);
        List<Font> fonts = fontRepository.findTop5ByMemberIdOrderByCreatedAtDesc(memberId);
        log.debug("Service detail: Found {} fonts for progress display", fonts.size());

        List<FontProgressResponse> result = fonts.stream()
                .map(FontProgressResponse::from)
                .collect(Collectors.toList());
                
        log.info("Service completed: Retrieved {} font progress items", result.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Font getOrThrowById(Long id) {
        log.debug("Service executing: Fetching font with ID: {}", id);
        return fontRepository.findById(id).orElseThrow(() -> {
            log.error("Service error: Font not found with ID: {}", id);
            return new FontNotFoundException();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FontResponse> getFonts(Long memberId, int page, int size) {
        log.info("Service executing: Fetching fonts for member ID: {}, page: {}, size: {}", memberId, page, size);
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Font> fontPage = fontRepository.findAllByMemberIdAndStatus(memberId, pageRequest, FontStatus.DONE);
        log.debug("Service detail: Found {} fonts on page {} for member ID: {}", 
                fontPage.getNumberOfElements(), page, memberId);

        Page<FontResponse> result = fontPage.map(font -> {
            boolean bookmarked = bookmarkRepository.existsByMemberIdAndFontId(memberId, font.getId());
            Member writer = memberLookupService.getOrThrowById(font.getMemberId());
            String woff2Url = cloudStorageService.getWoff2Url(font.getKey());
            return FontResponse.from(font, bookmarked, writer.getNickname(), woff2Url);
        });

        
        log.info("Service completed: Retrieved {} fonts out of {} total for member ID: {}", 
                result.getNumberOfElements(), result.getTotalElements(), memberId);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public FontResponse getFont(Long fontId, Long memberId) {
        log.info("Service executing: Fetching font details for font ID: {}", fontId);
        Font targetFont = getOrThrowById(fontId);
        checkFontStatusIsDone(targetFont);
        Member writer = memberLookupService.getOrThrowById(targetFont.getMemberId());

        boolean isBookmarked = false;
        if (memberId != null) {
            isBookmarked = bookmarkRepository.existsByMemberIdAndFontId(memberId, fontId);
        }
        String woff2Url = cloudStorageService.getWoff2Url(targetFont.getKey());
        FontResponse fontResponse = FontResponse.from(targetFont, isBookmarked, writer.getNickname(), woff2Url);

        log.info("Service completed: Retrieved font details for font ID: {} with name: {}", 
                fontId, targetFont.getName());

        return fontResponse;
    }

    @Override
    @Transactional
    public FontDeleteResponse delete(Long memberId, Long fontId) {
        log.info("Service executing: Deleting font ID: {} for member ID: {}", fontId, memberId);
        Member member = memberLookupService.getOrThrowById(memberId);
        Font targetFont = getOrThrowById(fontId);

        checkFontStatusIsDone(targetFont);
        checkFontOwnership(member.getId(), targetFont.getMemberId());

        fontRepository.deleteById(targetFont.getId());
        log.info("Service completed: Font ID: {} deleted successfully", fontId);

        return FontDeleteResponse.from(fontId);
    }

    @Override
    public Page<FontPageResponse> getFontPage(Long memberId, int page, int size, String sortBy, String keyword) {
        log.info("Service executing: Fetching font page with memberId: {}, page: {}, size: {}, sortBy: {}, keyword: {}", 
                memberId, page, size, sortBy, keyword);
                
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        if ("downloadCount".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Order.desc("downloadCount"));
        } else if ("bookmarkCount".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Order.desc("bookmarkCount"));
        }

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Font> fontPage;
        if (!StringUtils.hasText(keyword)) {
            fontPage = fontRepository.findAllByStatus(pageRequest, FontStatus.DONE);
        } else {
            log.debug("Service detail: Searching fonts with keyword: {}", keyword);
            fontPage = fontRepository.findByNameContainingAndStatus(keyword, pageRequest, FontStatus.DONE);
        }
        
        log.debug("Service detail: Found {} fonts on page {}", fontPage.getNumberOfElements(), page);

        Page<FontPageResponse> result;
        if (memberId == null) {
            result = fontPage.map(font -> {
                Member member = memberLookupService.getOrThrowById(font.getMemberId());
                String woff2Url = cloudStorageService.getWoff2Url(font.getKey());
                return FontPageResponse.from(font, member.getNickname(), false, woff2Url);
            });
        } else {
            result = fontPage.map(font -> {
                Member member = memberLookupService.getOrThrowById(font.getMemberId());
                boolean bookmarked = bookmarkRepository.existsByMemberIdAndFontId(memberId, font.getId());
                String woff2Url = cloudStorageService.getWoff2Url(font.getKey());
                return FontPageResponse.from(font, member.getNickname(), bookmarked, woff2Url);
            });
        }
        
        log.info("Service completed: Retrieved {} fonts out of {} total, {} pages", 
                result.getNumberOfElements(), result.getTotalElements(), result.getTotalPages());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FontResponse> getOtherFonts(Long fontId) {
        log.info("Service executing: Fetching other fonts from the same creator for font ID: {}", fontId);
        Font font = getOrThrowById(fontId);

        checkFontStatusIsDone(font);

        Member member = memberLookupService.getOrThrowById(font.getMemberId());

        List<Font> fonts = fontRepository.findTop3ByMemberIdAndIdNotAndStatusOrderByCreatedAtDesc(member.getId(), fontId, FontStatus.DONE);
        log.debug("Service detail: Found {} other fonts from the same creator", fonts.size());

        List<FontResponse> result = fonts.stream()
                .map(f -> {
                    boolean bookmarked = bookmarkRepository.existsByMemberIdAndFontId(member.getId(), f.getId());
                    Member writer = memberLookupService.getOrThrowById(f.getMemberId());
                    String woff2Url = cloudStorageService.getWoff2Url(font.getKey());
                    return FontResponse.from(f, bookmarked, writer.getNickname(), woff2Url);
                })
                .collect(Collectors.toList());
                
        log.info("Service completed: Retrieved {} other fonts from creator of font ID: {}", result.size(), fontId);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FontResponse> getMyPopularFonts(Long memberId) {
        log.info("Service executing: Fetching popular fonts for member ID: {}", memberId);
        Member member = memberLookupService.getOrThrowById(memberId);

        List<Font> fonts = fontRepository.findTop4ByMemberIdAndStatusOrderByDownloadAndBookmarkCountDesc(memberId, FontStatus.DONE);
        log.debug("Service detail: Found {} popular fonts for member ID: {}", fonts.size(), memberId);

        List<FontResponse> result = fonts.stream()
                .map(font -> {
                    boolean bookmarked = bookmarkRepository.existsByMemberIdAndFontId(member.getId(), font.getId());
                    Member writer = memberLookupService.getOrThrowById(font.getMemberId());
                    String woff2Url = cloudStorageService.getWoff2Url(font.getKey());
                    return FontResponse.from(font, bookmarked, writer.getNickname(), woff2Url);
                })
                .collect(Collectors.toList());
                
        log.info("Service completed: Retrieved {} popular fonts for member ID: {}", result.size(), memberId);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FontResponse> getPopularFonts(Long memberId) {
        log.info("Service executing: Fetching global popular fonts, requesting member ID: {}", memberId);
        List<Font> fonts = fontRepository.findTop3ByStatusOrderByDownloadAndBookmarkCountDesc(FontStatus.DONE);
        log.debug("Service detail: Found {} popular fonts globally", fonts.size());

        List<FontResponse> result;
        if (memberId == null) {
            result = fonts.stream()
                    .map(font -> {
                        Member writer = memberLookupService.getOrThrowById(font.getMemberId());
                        String woff2Url = cloudStorageService.getWoff2Url(font.getKey());
                        return FontResponse.from(font, false, writer.getNickname(), woff2Url);
                    })
                    .collect(Collectors.toList());
        } else {
            Member member = memberLookupService.getOrThrowById(memberId);
            result = fonts.stream()
                    .map(font -> {
                        boolean bookmarked = bookmarkRepository.existsByMemberIdAndFontId(member.getId(), font.getId());
                        Member writer = memberLookupService.getOrThrowById(font.getMemberId());
                        String woff2Url = cloudStorageService.getWoff2Url(font.getKey());
                        return FontResponse.from(font, bookmarked, writer.getNickname(), woff2Url);
                    })
                    .collect(Collectors.toList());
        }
        
        log.info("Service completed: Retrieved {} globally popular fonts", result.size());
        return result;
    }

    @Override
    @Transactional
    public FontUpdateResponse updateProgress(Long fontId, FontProgressUpdateDTO fontProgressUpdateDTO) {
        log.info("Service executing: Updating font ID: {}", fontId);
        Font targetFont = getOrThrowById(fontId);

        Font updatedFont = fontRepository.save(targetFont.updateProgress(fontProgressUpdateDTO));
        String woff2Url = cloudStorageService.getWoff2Url(updatedFont.getKey());

        if (fontProgressUpdateDTO.getStatus() == FontStatus.DONE) {
            String phoneNumber = phoneNumberStorage.getPhoneNumber(targetFont);

            if (phoneNumber != null && !phoneNumber.isBlank()) {
                smsService.sendFontProgressNotification(phoneNumber, updatedFont.getName());
                phoneNumberStorage.removePhoneNumber(targetFont);
            }
        }

        log.info("Service completed: Font ID: {} updated successfully", fontId);
        return FontUpdateResponse.from(updatedFont, woff2Url);
    }

    @Override
    @Transactional
    public FontDownloadResponse fontDownload(Long memberId, Long fontId) {
        log.info("Service executing: Download font ID: {}", fontId);
        Font targetFont = getOrThrowById(fontId);

        checkFontStatusIsDone(targetFont);

        targetFont.increaseDownloadCount();
        String ttfUrl = cloudStorageService.getTtfUrl(targetFont.getKey());
        fontRepository.save(targetFont);

        log.info("Service completed: Font ID: {} download successfully", fontId);

        return FontDownloadResponse.from(targetFont, ttfUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isDuplicateNameExists(Long memberId, String fontName) {
        return fontRepository.existsByName(fontName);
    }

    private void checkFontOwnership(Long requestMemberId, Long targetMemberId) {
        log.debug("Service detail: Checking font ownership: requestMemberId={}, targetMemberId={}", 
                requestMemberId, targetMemberId);
                
        if (!requestMemberId.equals(targetMemberId)) {
            log.warn("Service warning: Font ownership mismatch: requestMemberId={}, targetMemberId={}", 
                    requestMemberId, targetMemberId);
            throw new FontOwnerMismatchException();
        }
    }

    private void checkFontStatusIsDone(Font targetFont) {
        log.debug("Service detail: Checking font status is DONE: targetFontId={}", targetFont.getId());

        if (targetFont.getStatus() != FontStatus.DONE) {
            log.warn("Service warning: Font status is not DONE: targetFontId={}", targetFont.getId());
            throw new FontInvalidStatusException();
        }
    }

    private void checkFontNameAndExampleContainsBadWord(String name, String engName, String example) {
        log.debug("Service detail: Checking bad word: name={}, engName={} example={}", name, engName, example);

        if (badWordFiltering.blankCheck(name) || badWordFiltering.blankCheck(engName) || badWordFiltering.blankCheck(example)) {
            log.warn("Service warning: Font contains bad word: name={}, engName={}, example={}", name, engName, example);
            throw new FontContainsBadWordException();
        }
    }
}
