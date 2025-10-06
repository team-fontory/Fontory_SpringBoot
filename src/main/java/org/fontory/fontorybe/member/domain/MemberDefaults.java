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
    private Gender gender;

    @ConstructorBinding
    public MemberDefaults(LocalDate birth) {
        this.birth = birth;
        this.gender = Gender.NONE;
    }
}
