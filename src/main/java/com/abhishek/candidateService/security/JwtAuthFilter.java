package com.abhishek.candidateService.security;

import com.abhishek.candidateService.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }


        final String token = authHeader.substring(7);
        try {
            Claims claims = jwtUtil.parseClaims(token);
            String userId = claims.getSubject();
            Object rolesObj = claims.get("roles");


            Collection<SimpleGrantedAuthority> authorities = extractAuthorities(rolesObj);


            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("principalId", userId);
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
        }


        chain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private Collection<SimpleGrantedAuthority> extractAuthorities(Object rolesObj) {
        if (rolesObj == null) return List.of();
        if (rolesObj instanceof List<?>) {
            return ((List<?>) rolesObj).stream().map(Object::toString).map(r -> new SimpleGrantedAuthority("ROLE_" + r)).collect(Collectors.toList());
        } else {
            String rolesStr = rolesObj.toString();
            return List.of(rolesStr.split(",")).stream().map(String::trim).filter(s -> !s.isEmpty()).map(r -> new SimpleGrantedAuthority("ROLE_" + r)).collect(Collectors.toList());
        }
    }
}