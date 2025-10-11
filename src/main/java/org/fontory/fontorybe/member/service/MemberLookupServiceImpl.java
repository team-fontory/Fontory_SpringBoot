package org.fontory.fontorybe.member.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.exception.MemberNotFoundException;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Builder
@Service
@RequiredArgsConstructor
public class MemberLookupServiceImpl implements MemberLookupService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public Member getOrThrowById(Long id) {
        if (id == null) {
            throw new MemberNotFoundException();
        }
        return memberRepository.findById(id)
                .orElseThrow(MemberNotFoundException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNickname(String targetName) {
        return memberRepository.existsByNickname(targetName);
    }
}
