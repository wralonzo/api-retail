package com.wralonzo.detail_shop.security.filter;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.wralonzo.detail_shop.security.jwt.JwtUtil;

import org.springframework.beans.factory.annotation.Value;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtUtil jwtUtil;
  private final UserDetailsService userDetailsService;

  @Value("${app.frontend.url:http://localhost:4200}")
  private String frontendUrl;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    // Google nos devuelve el email en .getName()
    String email = authentication.getName();
    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

    // Generamos el token
    String token = jwtUtil.generateToken(userDetails);

    // Redirigimos a Angular
    String targetUrl = frontendUrl + "/oauth2/redirect?token=" + token;

    clearAuthenticationAttributes(request);
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }
}