package org.fontory.fontorybe.member.infrastructure;

import java.util.Optional;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.infrastructure.entity.MemberEntity;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {
    private final MemberJpaRepository memberJpaRepository;
    private final EntityManager em;

    @Override
    public Optional<Member> findById(Long id) {
        return memberJpaRepository.findById(id).map(MemberEntity::toModel);
    }

    @Override
    public Member save(Member member) {
        MemberEntity savedMember = memberJpaRepository.save(MemberEntity.from(member));

        /**
         * For Adapt BaseEntity
         */
        em.flush();
        em.refresh(savedMember);

        return savedMember.toModel();
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return memberJpaRepository.existsByNickname(nickname);
    }
}
