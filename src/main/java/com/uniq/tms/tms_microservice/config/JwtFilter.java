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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger logger = LogManager.getLogger(JwtFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String jwtToken = null;
        logger.info("fetch user agent from header");
        String userAgent = httpRequest.getHeader("User-Agent");
        String ipAddress = jwtUtil.getClientIp(httpRequest);
        logger.info("fetch jwt token from header");
        String authHeader = httpRequest.getHeader("Authorization");
        logger.debug("JWT_Filter **********userAgent from the token: {}"+ userAgent);
        logger.debug("JWT_Filter *************IP Address from the token: {}"+ ipAddress);
        logger.debug("JWT_Filter ************AuthHeader from the token: {}"+ authHeader);
       
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            logger.info("jwt token found in header");
            jwtToken = authHeader.substring(7);
        }

        if (jwtToken == null && httpRequest.getCookies() != null) {
            logger.info("fetch jwt token from cookie");
            for (Cookie cookie : httpRequest.getCookies()) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    jwtToken = cookie.getValue();
                    break;
                }
            }
        }

        if (jwtToken != null) {
            try {
                logger.info("parsing jwt token");
                Claims claims = jwtUtil.parseToken(jwtToken, userAgent);
                String email = claims.getSubject();
                String role = claims.get("roles", String.class);
                if (email != null && role != null) {
                    logger.info("find user by email from token");
                    UserEntity user = userRepository.findByEmail(email);
                    if (user == null) {
                        logger.info("user does not exist");
                        httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User does not exist");
                        return;
                    }
                    logger.info("fetching user role");
                    RoleEntity roleEntity = user.getRole();
                    String userRole = roleEntity.getName();

                    if (userRole.startsWith("ROLE_")) {
                        userRole = userRole.substring(5);
                    }
                    logger.info("setting authentication token");
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(email, null,
                                    Collections.singletonList(new SimpleGrantedAuthority(userRole)));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("authentication token set");
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("Invalid token",e.getMessage());
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
