package org.fontory.fontorybe.member.domain;

import lombok.Getter;
import lombok.Setter;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.time.LocalDate;

@Getter
@Setter
@ConfigurationProperties(prefix="member.default")
public class MemberDefaults {
    private LocalDate birth;
    private boolean termsAgree;
    private String profileImageUrl;
    private Gender gender;

    @ConstructorBinding
    public MemberDefaults(LocalDate birth, boolean termsAgree, String profileImageUrl) {
        this.birth = birth;
        this.termsAgree = termsAgree;
        this.profileImageUrl = profileImageUrl;
        this.gender = Gender.NONE;
    }

    public boolean getTerms() {
        return this.termsAgree;
    }

}
