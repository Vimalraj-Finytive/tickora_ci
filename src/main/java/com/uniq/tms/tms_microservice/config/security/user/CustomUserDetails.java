package com.uniq.tms.tms_microservice.config.security.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final String userId;
    private final String orgId;
    private final String role;
    private final String userName;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(String userId, String orgId, String role, String userName, String password,
                             Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.orgId = orgId;
        this.role = role;
        this.userName = userName;
        this.password = password;
        this.authorities = authorities;
    }

    public String getUserId() { return userId; }
    public String getOrgId() { return orgId; }
    public String getRole() { return role; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getUsername() { return userName; }
    @Override public String getPassword() { return password; }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
