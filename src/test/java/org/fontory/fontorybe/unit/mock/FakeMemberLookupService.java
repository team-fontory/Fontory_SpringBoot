package org.fontory.fontorybe.unit.mock;

import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.exception.MemberNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakeMemberLookupService implements MemberLookupService {
    private final Map<Long, Member> members = new HashMap<>();

    @Override
    public Member getOrThrowById(Long id) {
        Member member = members.get(id);
        if (member == null) {
            throw new MemberNotFoundException();
        }
        return member;
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return members.values().stream()
                .anyMatch(member -> member.getNickname().equals(nickname));
    }

    @Override
    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(members.get(id));
    }

    // Test helper methods
    public void addMember(Member member) {
        members.put(member.getId(), member);
    }

    public void removeMember(Long memberId) {
        members.remove(memberId);
    }

    public void reset() {
        members.clear();
    }

    public int getMemberCount() {
        return members.size();
    }
}