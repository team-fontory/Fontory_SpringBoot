package org.fontory.fontorybe.unit.mock;

import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.service.port.MemberRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeMemberRepository implements MemberRepository {
    private final AtomicLong authGeneratedID = new AtomicLong(0);
    private final List<Member> data = new ArrayList<>();


    @Override
    public Member save(Member member) {
        LocalDateTime now = LocalDateTime.now();
        if (member.getId() == null || member.getId() == 0) {
            Member newMember = Member.builder()
                    .id(authGeneratedID.incrementAndGet())
                    .nickname(member.getNickname())
                    .gender(member.getGender())
                    .birth(member.getBirth())
                    .profileImageKey(member.getProfileImageKey())
                    .provideId(member.getProvideId())
                    .status(member.getStatus())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            data.add(newMember);
            return newMember;
        } else {
            data.removeIf(m -> m.getId().equals(member.getId()));
            Member newMember = Member.builder()
                    .id(member.getId())
                    .nickname(member.getNickname())
                    .gender(member.getGender())
                    .birth(member.getBirth())
                    .profileImageKey(member.getProfileImageKey())
                    .provideId(member.getProvideId())
                    .status(member.getStatus())
                    .createdAt(member.getCreatedAt())
                    .updatedAt(now)
                    .deletedAt(member.getDeletedAt())
                    .build();
            data.add(newMember);
            return newMember;
        }
    }

    @Override
    public Optional<Member> findById(Long id) {
        return data.stream()
                .filter(item -> item.getId().equals(id))
                .findAny();
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return data.stream()
                .anyMatch(item -> item.getNickname().equals(nickname));
    }
}
