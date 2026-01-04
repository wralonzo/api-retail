package com.wralonzo.detail_shop.infrastructure.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("config")
@RequiredArgsConstructor
public class GoogleIDKeyController {

  @Value("${spring.security.oauth2.client.registration.google.client-id}")
  private String googleClientId;

  @GetMapping("/google-client-id")
  public ResponseEntity<Map<String, String>> getGoogleClientId() {
    return ResponseEntity.ok(Map.of("clientId", googleClientId));
  }

}
