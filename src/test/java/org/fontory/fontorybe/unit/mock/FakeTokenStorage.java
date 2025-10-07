package org.fontory.fontorybe.unit.mock;

import org.fontory.fontorybe.authentication.application.port.TokenStorage;
import org.fontory.fontorybe.member.domain.Member;

import java.util.HashMap;
import java.util.Map;

public class FakeTokenStorage implements TokenStorage {
    private final Map<Long, String> memberIdToRefreshToken = new HashMap<>();

    @Override
    public void saveRefreshToken(Member member, String refreshToken) {
        memberIdToRefreshToken.put(member.getId(), refreshToken);
    }

    @Override
    public void removeRefreshToken(Member member) {
        memberIdToRefreshToken.remove(member.getId());
    }

    @Override
    public String getRefreshToken(Member member) {
        return memberIdToRefreshToken.get(member.getId());
    }

    // Test helper methods
    public void reset() {
        memberIdToRefreshToken.clear();
    }

    public boolean hasRefreshToken(Long memberId) {
        return memberIdToRefreshToken.containsKey(memberId);
    }

    public Map<Long, String> getAllTokens() {
        return new HashMap<>(memberIdToRefreshToken);
    }

    public int getTokenCount() {
        return memberIdToRefreshToken.size();
    }
}