package org.fontory.fontorybe.member.service;

import com.vane.badwordfiltering.BadWordFiltering;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.domain.exception.MemberContainsBadWordException;
import org.fontory.fontorybe.member.domain.exception.MemberDuplicateNameExistsException;
import org.springframework.stereotype.Service;

/**
 * 회원 관련 검증 로직을 중앙화한 서비스
 */
@Service
@RequiredArgsConstructor
public class MemberValidationService {
    private final BadWordFiltering badWordFiltering;
    private final MemberLookupService memberLookupService;
    
    /**
     * 닉네임에 금지 단어가 포함되어 있는지 검사
     * @param nickname 검사할 닉네임
     * @throws MemberContainsBadWordException 금지 단어가 포함된 경우
     */
    public void validateNoBadWords(String nickname) {
        if (badWordFiltering.blankCheck(nickname)) {
            throw new MemberContainsBadWordException();
        }
    }
    
    /**
     * 닉네임 중복 여부를 검사
     * @param nickname 검사할 닉네임
     * @throws MemberDuplicateNameExistsException 중복된 닉네임인 경우
     */
    public void validateNicknameNotDuplicated(String nickname) {
        if (memberLookupService.existsByNickname(nickname)) {
            throw new MemberDuplicateNameExistsException();
        }
    }
    
    /**
     * 닉네임 변경 시 중복 검사 (현재 닉네임과 다른 경우에만)
     * @param currentNickname 현재 닉네임
     * @param newNickname 변경할 닉네임
     * @throws MemberDuplicateNameExistsException 중복된 닉네임인 경우
     */
    public void validateNicknameChangeNotDuplicated(String currentNickname, String newNickname) {
        if (!currentNickname.equals(newNickname) && memberLookupService.existsByNickname(newNickname)) {
            throw new MemberDuplicateNameExistsException();
        }
    }
    
    /**
     * 닉네임 전체 유효성 검사 (금지 단어 + 중복)
     * @param nickname 검사할 닉네임
     */
    public void validateNickname(String nickname) {
        validateNoBadWords(nickname);
        validateNicknameNotDuplicated(nickname);
    }
}