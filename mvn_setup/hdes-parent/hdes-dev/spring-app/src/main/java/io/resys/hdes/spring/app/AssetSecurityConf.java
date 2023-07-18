package io.resys.hdes.spring.app;

/*-
 * #%L
 * spring-app
 * %%
 * Copyright (C) 2020 - 2022 Copyright 2020 ReSys OÜ
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

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;


/**
 * CsrfToken example
 */
@Configuration
@ConditionalOnProperty(name = "wrench.assets.enabled", havingValue = "true", matchIfMissing = true)
public class AssetSecurityConf {

  /** disables security even if configured
  private static final Logger LOGGER = LoggerFactory.getLogger(AssetSecurityConf.class);
  @Value("${wrench.cors.allowedorigins:http://localhost:3000}")
  private String[] allowedorigins;
  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        LOGGER.debug("ALLOWED ORIGINS " + String.join(", ", allowedorigins));
        
        String mapping = "/**";
        registry.addMapping(mapping).allowedMethods("*")
          .allowCredentials(true)
          .allowedOrigins(allowedorigins);
      }
    };
  }

  @Bean
  public WebSecurityConfigurerAdapter permitAll() {
    return new WebSecurityConfigurerAdapter() {
      @Override
      protected void configure(HttpSecurity http) throws Exception{
        http.csrf().disable().anonymous().and().authorizeRequests().anyRequest().permitAll();
      }
    };
  }


  
  @Bean
  @ConditionalOnMissingBean(HdesCredsSupplier.class)
  @ConditionalOnProperty(value = "security.oauth2.enabled", havingValue = "true")
  public HdesCredsSupplier springSecurityAssetAuthorProvider(GitConfigBean gitConfigBean) {
    return new AuthorProvider(gitConfigBean);
  }

  @Bean
  @ConditionalOnMissingBean(SpringIdeTokenSupplier.class)
  @ConditionalOnProperty(value = "security.oauth2.enabled", havingValue = "true")
  public SpringIdeTokenSupplier springIdeTokenSupplier() {
    return (re) -> {
      CsrfToken csrfToken = (CsrfToken) re.getAttribute(CsrfToken.class.getName());
      if (csrfToken == null) {
        return Optional.empty();
      }
      return Optional.of(ImmutableIdeToken.builder().key(csrfToken.getHeaderName()).value(csrfToken.getToken()).build());
    };
  }
  public static class AuthorProvider implements HdesCredsSupplier {
    private final GitConfigBean gitConfigBean;
    public AuthorProvider(GitConfigBean gitConfigBean) {
      this.gitConfigBean = gitConfigBean;
    }
    @Override
    public HdesCreds get() {
      SecurityContext securityContext = SecurityContextHolder.getContext();
      Authentication authentication = securityContext.getAuthentication();
      String email = gitConfigBean.getEmail();
      String name = null;
      if(authentication != null) {
        Object principal  = authentication.getPrincipal();
        if (principal instanceof OAuth2User) {
          OAuth2User user = (OAuth2User) principal;
          Map<String, Object> decodedDetails = user.getAttributes();
          email = (String) decodedDetails.get("email");
          name = StringUtils.defaultIfBlank((String) decodedDetails.get("given_name"), "") + " " + StringUtils.defaultIfBlank((String) decodedDetails.get("family_name"), "");
        }
      }
      if (StringUtils.isBlank(name) && email.contains("@")) {
        name = email.split("@")[0];
      }
      return ImmutableHdesCreds.builder().user(name).email(email).build();
    }
  }

  
  */
}
