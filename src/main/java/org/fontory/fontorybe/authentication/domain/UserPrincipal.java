package org.fontory.fontorybe.authentication.domain;

import lombok.Data;
import lombok.Getter;
import org.fontory.fontorybe.member.domain.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@Getter
public class UserPrincipal implements UserDetails {
    private Long id;

    public UserPrincipal(Long id) {
        this.id = id;
    }

    public static UserPrincipal from(Member member) {
        return new UserPrincipal(member.getId());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return String.valueOf(getId());
    }
}
