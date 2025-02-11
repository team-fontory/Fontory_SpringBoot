package org.fontory.fontorybe.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.member.controller.dto.*;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "회원관리", description = "사용자 API")
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final ProvideService provideService;

    @Operation(
            summary = "닉네임 중복 확인",
            description = "주어진 닉네임으로 생성되어있는 사용자가 있는지 검색 후 결과를 반환합니다."
    )
    @GetMapping("/check-duplicate")
    public ResponseEntity<Boolean> checkDuplicate(
            @RequestParam String nickname
    ) {
        Boolean duplicateNameExists = memberService.isDuplicateNameExists(nickname);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(duplicateNameExists);
    }

    @Operation(
            summary = "회원가입"
    )
    @PostMapping
    public ResponseEntity<MemberCreateResponse> addMember(
            @RequestBody MemberCreate memberCreate
    ) {
        // not implements yet
        Long tempProvideId = provideService.getTempProvideId();

        Member createdMember = memberService.create(memberCreate, tempProvideId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(MemberCreateResponse.from(createdMember));
    }

    @Operation(
            summary = "회원정보 수정"
    )
    @Parameter(name = "memberId", description = "수정할 회원 ID")
    @PutMapping("/{memberId}")
    public ResponseEntity<MemberUpdateResponse> updateMember(
        @RequestBody MemberUpdate memberUpdate,
        @PathVariable Long memberId
    ) {
        Long requestMemberId = memberId;

//        should be tested
//        Member updatedMember = memberService.update(requestMemberId, memberId, memberUpdate);

        Member updatedMember = memberService.update(requestMemberId, memberId, memberUpdate);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(MemberUpdateResponse.from(updatedMember));
    }

    @Operation(
            summary = "회원탈퇴"
    )
    @Parameter(name = "memberId", description = "탈퇴할 회원 ID")
    @DeleteMapping("/{memberId}")
    public ResponseEntity<MemberDisableResponse> disableMember(
            @PathVariable Long memberId
    ) {
        Long requestMemberId = memberId;

//        should be tested
//        Member updatedMember = memberService.update(requestMemberId, memberId, memberUpdate);
        Member disabledMember = memberService.disable(requestMemberId, memberId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(MemberDisableResponse.from(disabledMember));
    }
}
