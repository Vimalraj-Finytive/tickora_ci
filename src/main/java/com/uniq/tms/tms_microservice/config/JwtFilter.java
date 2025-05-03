package com.uniq.tms.tms_microservice.config;

import com.uniq.tms.tms_microservice.entity.RoleEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter implements Filter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Autowired
    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String jwtToken = null;
        String userAgent = httpRequest.getHeader("User-Agent");
        String ipAddress = jwtUtil.getClientIp(httpRequest);
        String authHeader = httpRequest.getHeader("Authorization");
        System.out.println("JWT_Filter **********userAgent from the token: {}"+ userAgent);
        System.out.println("JWT_Filter *************IP Address from the token: {}"+ ipAddress);
        System.out.println("JWT_Filter ************AuthHeader from the token: {}"+ authHeader);
       
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
        }

        if (jwtToken == null && httpRequest.getCookies() != null) {
            for (Cookie cookie : httpRequest.getCookies()) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    jwtToken = cookie.getValue();
                    break;
                }
            }
        }

        if (jwtToken != null) {
            try {
                Claims claims = jwtUtil.parseToken(jwtToken, userAgent, ipAddress);
                String email = claims.getSubject();
                String role = claims.get("roles", String.class);
                if (email != null && role != null) {
                    UserEntity user = userRepository.findByEmail(email);
                    if (user == null) {
                        httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User does not exist");
                        return;
                    }

                    RoleEntity roleEntity = user.getRole();
                    String userRole = roleEntity.getName();

                    if (userRole.startsWith("ROLE_")) {
                        userRole = userRole.substring(5);
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(email, null,
                                    Collections.singletonList(new SimpleGrantedAuthority(userRole)));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                e.printStackTrace();
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
