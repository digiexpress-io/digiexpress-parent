package io.thestencil.quarkus.iam;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigMapping
@ConfigRoot(phase = ConfigPhase.RUN_TIME, name = IAMRecorder.FEATURE_BUILD_ITEM)
public interface RuntimeConfig {
  
  
  /**
   * Configuration for security proxy from where to get person roles
   */
  PersonSecurityProxyConfig personSecurityProxy();
  
  
  /**
   * Configuration for security proxy from where to get company roles
   */
  PersonSecurityProxyConfig companySecurityProxy();
}
