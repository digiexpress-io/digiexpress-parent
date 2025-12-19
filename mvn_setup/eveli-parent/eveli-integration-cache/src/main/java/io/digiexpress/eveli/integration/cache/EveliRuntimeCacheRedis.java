package io.digiexpress.eveli.integration.cache;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;

import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliRuntime;
import io.digiexpress.eveli.envir.spi.actions.EveliRuntimeCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;




@Slf4j
@RequiredArgsConstructor
public class EveliRuntimeCacheRedis implements EveliRuntimeCache {
  private final RedisTemplate<String, EveliDeployment> deploymentCache;
  private final RedisTemplate<String, EveliRuntime> runtimeCache;
  private final Duration shortDeploymentTtl;
  private final Duration longRuntimeTtl;

  @Override
  public Optional<EveliDeployment> getDeployment() {
    final OffsetDateTime now = OffsetDateTime.now();
    
    // Get all deployment keys
    Set<String> keys = deploymentCache.keys("*");
    if (keys == null || keys.isEmpty()) {
      return Optional.empty();
    }
    
    // Fetch all deployments
    List<EveliDeployment> deployments = deploymentCache.opsForValue().multiGet(keys);
    if (deployments == null) {
      return Optional.empty();
    }
    
    final var result = deployments.stream()
      .filter(Objects::nonNull)
      .filter(a -> a.getStartsAt().isBefore(now))
      .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
      .findFirst();
      
    log.debug("Redis cache, resolving deployment: {} from cache", result.map(EveliDeployment::getId));
    return result;
  }

  @Override
  public Optional<EveliRuntime> getRuntime(String runtimeId) {
    final var runtime = Optional.ofNullable(runtimeCache.opsForValue().get(runtimeId));
    log.debug("Redis cache, resolving runtime: {} from cache", runtime.map(EveliRuntime::getDeploymentId));
    return runtime;
  }

  @Override
  public EveliDeployment save(EveliDeployment deployment) {
    deploymentCache.opsForValue().set(
        deployment.getId(), 
        deployment, 
        shortDeploymentTtl
    );
    log.debug("Redis cache, saving deployment: {} to cache", deployment.getId());
    return deployment;
  }

  @Override
  public EveliRuntime save(EveliRuntime runtime) {
    runtimeCache.opsForValue().set(
        runtime.getDeploymentId(), 
        runtime, 
        longRuntimeTtl
    );
    log.debug("Redis cache, saving runtime: {} from cache", runtime.getDeploymentId());
    return runtime;
  }

  public void invalidateId() {
    log.debug("Redis cache, invalidating deployment cache");
    Set<String> keys = deploymentCache.keys("*");
    if (keys != null && !keys.isEmpty()) {
      deploymentCache.delete(keys);
    }
  }

  public void invalidateAll() {
    log.debug("Redis cache, invalidating deployment cache");
    Set<String> deploymentKeys = deploymentCache.keys("*");
    if (deploymentKeys != null && !deploymentKeys.isEmpty()) {
      deploymentCache.delete(deploymentKeys);
    }

    log.debug("Redis cache, invalidating runtime cache");
    Set<String> runtimeKeys = runtimeCache.keys("*");
    if (runtimeKeys != null && !runtimeKeys.isEmpty()) {
      runtimeCache.delete(runtimeKeys);
    }
  }
}
