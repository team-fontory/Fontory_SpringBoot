package org.fontory.fontorybe.font.service;

import com.vane.badwordfiltering.BadWordFiltering;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.font.domain.exception.FontContainsBadWordException;
import org.fontory.fontorybe.font.domain.exception.FontDuplicateNameExistsException;
import org.fontory.fontorybe.font.service.port.FontRepository;
import org.springframework.stereotype.Service;

/**
 * 폰트 관련 검증 로직을 중앙화한 서비스
 */
@Service
@RequiredArgsConstructor
public class FontValidationService {
    private final BadWordFiltering badWordFiltering;
    private final FontRepository fontRepository;
    
    /**
     * 폰트 이름 중복 여부 검사
     * @param memberId 회원 ID
     * @param fontName 검사할 폰트 이름
     * @return 중복 여부
     */
    public boolean isDuplicateNameExists(Long memberId, String fontName) {
        return fontRepository.existsByMemberIdAndName(memberId, fontName);
    }
    
    /**
     * 폰트 이름 중복 검증 (예외 발생)
     * @param memberId 회원 ID
     * @param fontName 검사할 폰트 이름
     * @throws FontDuplicateNameExistsException 중복된 이름인 경우
     */
    public void validateFontNameNotDuplicated(Long memberId, String fontName) {
        if (isDuplicateNameExists(memberId, fontName)) {
            throw new FontDuplicateNameExistsException();
        }
    }
    
    /**
     * 텍스트에 금지 단어가 포함되어 있는지 검사
     * @param text 검사할 텍스트
     * @throws FontContainsBadWordException 금지 단어가 포함된 경우
     */
    public void validateNoBadWords(String text) {
        if (text != null && badWordFiltering.blankCheck(text)) {
            throw new FontContainsBadWordException();
        }
    }
    
    /**
     * 폰트 관련 텍스트들에 금지 단어가 포함되어 있는지 일괄 검사
     * @param fontName 폰트 이름
     * @param engName 영문 이름
     * @param example 예시 텍스트
     * @throws FontContainsBadWordException 금지 단어가 포함된 경우
     */
    public void validateFontTextsNoBadWords(String fontName, String engName, String example) {
        validateNoBadWords(fontName);
        validateNoBadWords(engName);
        validateNoBadWords(example);
    }
}