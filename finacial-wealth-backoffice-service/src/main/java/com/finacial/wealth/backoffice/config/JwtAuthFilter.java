package com.finacial.wealth.backoffice.config;

import com.finacial.wealth.backoffice.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    try {
      String h = request.getHeader("Authorization");
      if (h != null && h.startsWith("Bearer ")) {
        String token = h.substring("Bearer ".length());
        Claims c = jwtService.parse(token);

        String userId = c.getSubject();
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) c.get("roles");

        var authorities = roles == null ? List.<SimpleGrantedAuthority>of()
            : roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();

        var auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
      chain.doFilter(request, response);
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }
}
