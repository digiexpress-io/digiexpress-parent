package io.digiexpress.eveli.app.authentication;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.converter.RsaKeyConverters;
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
@Profile("jwt")
public class AuthenticationConfigJWT {
  @Bean
  public SpringJwtAuthClient authClientJwt() {
    return new SpringJwtAuthClient();
  }

  @Bean
  public SpringJwtCrmClient crmClientJwt() {
    return new SpringJwtCrmClient(new RestTemplate(), "");
  }

  @Value("${app.jwt.public-key-value}")
  private String publicKeyValue;
  @Value("${app.jwt.issuer}")
  private String issuer;
  @Value("${app.jwt.portal.public-key-value:#{null}}")
  private String portalPublicKeyValue;
  @Value("${app.jwt.portal.issuer}")
  private String portalIssuer;


  @Bean
  JwtIssuerAuthenticationManagerResolver authenticationManagerResolver() {
    Map<String, AuthenticationManager> decoders;
    decoders = Map.of(issuer, authenticationManager(jwtDecoder(publicKeyValue), jwtAuthenticationConverter()), portalIssuer,
        authenticationManager(jwtDecoder(portalPublicKeyValue), jwtPortalAuthenticationConverter()));
    return new JwtIssuerAuthenticationManagerResolver(decoders::get);
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


  private AuthenticationManager authenticationManager(JwtDecoder decoder, JwtAuthenticationConverter converter) {
    JwtAuthenticationProvider provider = new JwtAuthenticationProvider(decoder);
    if (converter != null) {
      provider.setJwtAuthenticationConverter(converter);
    }
    return new ProviderManager(provider);
  }

  private JwtDecoder jwtDecoder(String publicKeyValue) {
    RSAPublicKey rsaPublicKey = RsaKeyConverters.x509()
          .convert(new ByteArrayInputStream(publicKeyValue.replace("\\n", "\n").getBytes()));
    return NimbusJwtDecoder.withPublicKey(rsaPublicKey)
        .signatureAlgorithm(org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256).build();
  }

}
