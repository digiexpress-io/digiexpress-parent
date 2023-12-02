package io.resys.sysconfig.client.spi.executor;

import io.resys.sysconfig.client.api.ExecutorClient;
import io.resys.sysconfig.client.api.SysConfigClient.SysConfigReleaseQuery;
import io.resys.sysconfig.client.api.model.SysConfigRelease;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ExecutorClientImpl implements ExecutorClient {
  private final ExecutorStore store;
  
  @Override
  public SysConfigSessionQuery querySession() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SysConfigReleaseQuery queryReleases() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SysConfigSessionBuilder createSession() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SysConfigFillBuilder fillInstance() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SysConfigProcesssFillBuilder processFillInstance() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<SysConfigSession> save(SysConfigSession session) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<SysConfigRelease> save(SysConfigRelease release) {
    // TODO Auto-generated method stub
    return null;
  }

}
