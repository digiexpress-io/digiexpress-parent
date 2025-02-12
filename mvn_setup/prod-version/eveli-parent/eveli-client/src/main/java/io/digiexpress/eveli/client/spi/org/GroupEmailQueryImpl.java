package io.digiexpress.eveli.client.spi.org;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.digiexpress.eveli.client.api.OrgClient.GroupEmailQuery;
import io.digiexpress.eveli.client.spi.asserts.IntegrationAssert;
import io.digiexpress.eveli.client.spi.comms.EmailBuilderDelegate.EmailRequest;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GroupEmailQueryImpl implements GroupEmailQuery {
  private final RestTemplate client;
  private final String membershipUrl;
  private final GroupEmailQueryLogger logger = new GroupEmailQueryLogger();

  @Override
  public List<String> findAllByGroupName(String groupName) {
    try {
      
      logger.groupName(groupName);
      
      final var requestEntity = createRequest();
      final var serviceUrl = UriComponentsBuilder.fromHttpUrl(membershipUrl).queryParam("groupName", groupName).toUriString();
      logger.serviceUrl(serviceUrl);
      
      final var response = client.exchange(serviceUrl, HttpMethod.GET, requestEntity, String[].class);
      logger.response(response);
      
      final var result = createResult(response);
      logger.result(result);
      
      logger.info();
      return result;
    } catch (Exception e) {
      
      logger.error(e);
      throw IntegrationAssert.fail(e);
    }
  }
  
  
  private List<String> createResult(ResponseEntity<String[]> response) {
    IntegrationAssert.isTrue(response.getStatusCode().is2xxSuccessful(), () -> "Response status was: " + response.getStatusCode() + " but expecting 200!");
    final var emails = new HashSet<String>(Arrays.asList(response.getBody()));
    return emails.stream().toList();
  }
  
  private HttpEntity<?> createRequest() {
    final var headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    final HttpEntity<EmailRequest> requestEntity = new HttpEntity<>(headers);
    return requestEntity;
  }
  
}
