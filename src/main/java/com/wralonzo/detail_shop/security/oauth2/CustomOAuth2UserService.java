package com.wralonzo.detail_shop.security.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.wralonzo.detail_shop.application.repositories.UserRepository;
import com.wralonzo.detail_shop.domain.entities.User;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  @Autowired
  private UserRepository userRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);

    // Extraemos datos (Google usa 'email' y 'name', Facebook puede variar)
    String email = oAuth2User.getAttribute("email");
    String fullName = oAuth2User.getAttribute("name");
    String provider = userRequest.getClientRegistration().getRegistrationId(); // 'google' o 'facebook'

    // Lógica: Si el usuario no existe en nuestra DB, lo creamos
    userRepository.findByUsername(email).orElseGet(() -> {
      User newUser = new User();
      newUser.setUsername(email);
      newUser.setFullName(fullName);
      newUser.setEnabled(true);
      // client es null porque es un usuario que reservará
      return userRepository.save(newUser);
    });

    return oAuth2User;
  }
}