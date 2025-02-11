package org.fontory.fontorybe.unit.mock;

import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.service.MemberServiceImpl;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.service.ProvideServiceImpl;
import org.fontory.fontorybe.provide.service.port.ProvideRepository;

public class TestContainer {
    public final MemberRepository memberRepository;
    public final ProvideRepository provideRepository;

    public final ProvideService provideService;
    public final MemberService memberService;

    public TestContainer() {
        memberRepository = new FakeMemberRepository();
        provideRepository = new FakeProvideRepository();

        provideService = ProvideServiceImpl.builder()
                .provideRepository(provideRepository)
                .build();

        memberService = MemberServiceImpl.builder()
                .memberRepository(memberRepository)
                .provideService(provideService)
                .build();
    }
}
