package io.digiexpress.eveli.client.spi.org;

import org.springframework.web.client.RestTemplate;

import io.digiexpress.eveli.client.api.OrgClient;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class OrgClientImpl implements OrgClient {
  private final RestTemplate client;
  private final String membershipUrl;

  @Override
  public GroupEmailQuery queryGroupEmails() {
    return new GroupEmailQueryImpl(client, membershipUrl);
  }

}
