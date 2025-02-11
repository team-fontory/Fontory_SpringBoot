package org.fontory.fontorybe.member.infrastructure;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.infrastructure.entity.MemberEntity;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {
    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Optional<Member> findById(Long id) {
        return memberJpaRepository.findById(id).map(MemberEntity::toModel);
    }

    @Override
    public Member save(Member member) {
        return memberJpaRepository.save(MemberEntity.from(member))
                .toModel();
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return memberJpaRepository.existsByNickname(nickname);
    }
}
