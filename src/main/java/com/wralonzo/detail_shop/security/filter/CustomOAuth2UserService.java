package com.wralonzo.detail_shop.security.filter;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.wralonzo.detail_shop.modules.auth.domain.enums.ProviderRegister;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

/*   private final UserRepository userRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);
    return processOAuth2User(userRequest, oAuth2User);
  } */

 /*  private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
    String email = oAuth2User.getAttribute("email");
    String name = oAuth2User.getAttribute("name");
    String googleId = oAuth2User.getAttribute("sub");
    String provider = userRequest.getClientRegistration().getRegistrationId();

    // Buscamos al usuario. Si no existe, lo creamos.
    userRepository.findByUsername(email)
        .map(existingUser -> updateExistingUser(existingUser, name))
        .orElseGet(() -> registerNewUser(email, name, ProviderRegister.valueOf(provider.toUpperCase()), googleId));

    return oAuth2User;
  } */

 /*  private User registerNewUser(String email, String name, ProviderRegister provider, String providerId) {
    User user = User.builder()
        .username(email)
        .fullName(name)
        .provider(provider)
        .providerId(providerId)
        .enabled(true)
        .build();
    return userRepository.save(user);
  }

  private User updateExistingUser(User existingUser, String name) {
    existingUser.setFullName(name);
    return userRepository.save(existingUser);
  } */
}
