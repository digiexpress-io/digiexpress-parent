package io.thestencil.quarkus.feedback;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.List;

@ConfigMapping
@ConfigRoot(phase = ConfigPhase.RUN_TIME, name = FeedbackRecorder.FEATURE_BUILD_ITEM)
public interface RuntimeConfig {
  /**
   * Default locale for creating forms
   */
  @WithDefault("fi")
  String defaultLocale();
  
  /**
   * Process names allowed to be started
   */
  List<String> allowed();
  
  /**
   * User id for new process
   */  
  String userId();
  
  /**
   * User name for new process
   */  
  String userName();
  
  /**
   * User first name for new process
   */  
  String firstName();
  
  /**
   * User last name for new process
   */  
  String lastName();
  
  /**
   * User email for new process
   */  
  String email();
  
  /**
   * User address for new process
   */  
  String address();
  
  /**
   * Configuration for process management backend
   */
  ProcessesConfig processes();

  /**
   * Configuration for form filling backend
   */
  FillConfig fill();
}
