package org.fontory.fontorybe.member.controller.port;

import org.fontory.fontorybe.file.domain.FileUploadResult;
import org.fontory.fontorybe.member.controller.dto.InitMemberInfoRequest;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.provide.domain.Provide;

public interface MemberOnboardService {
    Member fetchOrCreateMember(Provide p);
    Member initNewMemberInfo(Long requestMemberId, InitMemberInfoRequest initMemberInfoRequest, FileUploadResult fileUploadResult);
    Member initNewMemberInfo(Long requestMemberId, InitMemberInfoRequest initMemberInfoRequest);
}
