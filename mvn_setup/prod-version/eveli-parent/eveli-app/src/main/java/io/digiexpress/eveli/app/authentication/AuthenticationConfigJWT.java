package io.digiexpress.eveli.app.authentication;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*-
 * #%L
 * eveli-app
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.web.client.RestTemplate;

import io.digiexpress.eveli.client.spi.auth.SpringJwtAuthClient;
import io.digiexpress.eveli.client.spi.auth.SpringJwtCrmClient;

@Configuration
public class AuthenticationConfigJWT {
  @Bean
  @Profile("jwt")
  public SpringJwtAuthClient authClientJwt() {
    return new SpringJwtAuthClient();
  }

  @Bean
  @Profile("jwt")
  public SpringJwtCrmClient crmClientJwt() {
    return new SpringJwtCrmClient(new RestTemplate(), "");
  }

  @Value("${app.jwt.secret}")
  private String secret;
  @Value("${app.jwt.issuer}")
  private String issuer;
  @Value("${app.jwt.portal.publicKey:#{null}}")
  private Resource portalPublicKey;
  @Value("${app.jwt.portal.public-key-value:#{null}}")
  private String portalPublicKeyValue;
  @Value("${app.jwt.portal.issuer}")
  private String portalIssuer;

  private final String PORTAL_USER_ROLE = "SCOPE_PortalUser";

  @Bean
  JwtIssuerAuthenticationManagerResolver authenticationManagerResolver() {
    Map<String, AuthenticationManager> decoders;
    try {
      decoders = Map.of(issuer, authenticationManager(jwtDecoder(), jwtAuthenticationConverter()), portalIssuer,
          authenticationManager(jwtPortalDecoder(), jwtPortalAuthenticationConverter()));
      return new JwtIssuerAuthenticationManagerResolver(decoders::get);
    } catch (IOException e) {
      throw new RuntimeException("Error in decoder creation", e);
    }
  }

  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  private JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    grantedAuthoritiesConverter.setAuthorityPrefix("");
    grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");

    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
    return jwtAuthenticationConverter;
  }

  private JwtAuthenticationConverter jwtPortalAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    grantedAuthoritiesConverter.setAuthoritiesClaimName("scope");

    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
    return jwtAuthenticationConverter;
  }

  private SecretKey createSecretKey() {
    return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
  }

  private AuthenticationManager authenticationManager(JwtDecoder decoder, JwtAuthenticationConverter converter) {
    JwtAuthenticationProvider provider = new JwtAuthenticationProvider(decoder);
    if (converter != null) {
      provider.setJwtAuthenticationConverter(converter);
    }
    return new ProviderManager(provider);
  }

  private JwtDecoder jwtDecoder() {
    SecretKey key = createSecretKey();
    return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS512).build();
  }

  private JwtDecoder jwtPortalDecoder() throws IOException {
    RSAPublicKey rsaPublicKey;
    if (StringUtils.isNotBlank(portalPublicKeyValue)) {
      rsaPublicKey = RsaKeyConverters.x509()
          .convert(new ByteArrayInputStream(portalPublicKeyValue.replace("\\n", "\n").getBytes()));
    } else {
      try (InputStream is = portalPublicKey.getInputStream()) {
        rsaPublicKey = RsaKeyConverters.x509().convert(new ByteArrayInputStream(is.readAllBytes()));
      }
    }
    return NimbusJwtDecoder.withPublicKey(rsaPublicKey)
        .signatureAlgorithm(org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256).build();
  }
}
