package org.fontory.fontorybe.member.service;

import com.vane.badwordfiltering.BadWordFiltering;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.file.application.port.FileService;
import org.fontory.fontorybe.file.domain.FileMetadata;
import org.fontory.fontorybe.file.domain.FileUploadResult;
import org.fontory.fontorybe.member.controller.dto.InitMemberInfoRequest;
import org.fontory.fontorybe.member.controller.port.MemberCreationService;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.controller.port.MemberOnboardService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyJoinedException;
import org.fontory.fontorybe.member.domain.exception.MemberContainsBadWordException;
import org.fontory.fontorybe.member.domain.exception.MemberDuplicateNameExistsException;
import org.fontory.fontorybe.member.infrastructure.entity.MemberStatus;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.fontory.fontorybe.provide.domain.Provide;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Builder
@Service
@RequiredArgsConstructor
public class MemberOnboardServiceImpl implements MemberOnboardService {
    private final MemberRepository memberRepository;
    private final MemberLookupService memberLookupService;
    private final MemberCreationService memberCreationService;
    private final FileService fileService;
    private final BadWordFiltering badWordFiltering;

    @Override
    @Transactional
    public Member fetchOrCreateMember(Provide p) {
        if (p.getMemberId()==null) {
            return memberCreationService.createDefaultMember(p);
        } else {
            return memberLookupService.getOrThrowById(p.getMemberId());
        }
    }

    @Override
    @Transactional
    public Member initNewMemberInfo(Long requestMemberId,
                                    InitMemberInfoRequest initNewMemberInfoRequest,
                                    FileUploadResult fileUploadResult) {
        Member targetMember = memberLookupService.getOrThrowById(requestMemberId);
        FileMetadata fileMetadata = fileService.getOrThrowById(fileUploadResult.getId());
        if (targetMember.getStatus() == MemberStatus.ACTIVATE) {
            throw new MemberAlreadyJoinedException();
        } else if (memberLookupService.existsByNickname(initNewMemberInfoRequest.getNickname())) {
            throw new MemberDuplicateNameExistsException();
        }

        checkContainsBadWord(initNewMemberInfoRequest.getNickname());

        return memberRepository.save(targetMember.initNewMemberInfo(initNewMemberInfoRequest, fileMetadata.getKey()));
    }

    private void checkContainsBadWord(String nickname) {
        if (badWordFiltering.blankCheck(nickname)) {
            throw new MemberContainsBadWordException();
        }
    }
}
