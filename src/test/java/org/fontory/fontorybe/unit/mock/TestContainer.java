package org.fontory.fontorybe.unit.mock;

import org.fontory.fontorybe.authentication.adapter.outbound.JwtTokenProvider;
import org.fontory.fontorybe.authentication.application.AuthService;
import org.fontory.fontorybe.authentication.application.TokenService;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.service.MemberServiceImpl;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.service.ProvideServiceImpl;
import org.fontory.fontorybe.provide.service.port.ProvideRepository;
import org.springframework.data.redis.core.RedisTemplate;

import static org.mockito.Mockito.mock;

public class TestContainer {
    public final String secretKeyFroTest = "d7582740ab56d2347710acc2fa15324012f7d24db45b86255b2f2f942a455ce880b5d370a85c49b84fac901e72fce1193986e9569e780128aa411b9c4cd3aec1";
    public final MemberRepository memberRepository;
    public final ProvideRepository provideRepository;

    public final ProvideService provideService;
    public final MemberService memberService;

    public final FakeRedisTemplate fakeRedisTemplate;

    public final TokenService tokenService;
    public final AuthService authService;

    public final JwtTokenProvider jwtTokenProvider;

    public TestContainer() {
        fakeRedisTemplate = new FakeRedisTemplate();
        jwtTokenProvider = new JwtTokenProvider(secretKeyFroTest);

        memberRepository = new FakeMemberRepository();
        provideRepository = new FakeProvideRepository();

        tokenService = new TokenService(jwtTokenProvider, fakeRedisTemplate);
        authService = new AuthService(tokenService, jwtTokenProvider);

        provideService = ProvideServiceImpl.builder()
                .provideRepository(provideRepository)
                .build();

        memberService = MemberServiceImpl.builder()
                .memberRepository(memberRepository)
                .provideService(provideService)
                .build();

//        jwtTokenProvider = JwtTokenProvider.builder()
//                .secretKey("d7582740ab56d2347710acc2fa15324012f7d24db45b86255b2f2f942a455ce880b5d370a85c49b84fac901e72fce1193986e9569e780128aa411b9c4cd3aec1")
//                .build();
    }
}
