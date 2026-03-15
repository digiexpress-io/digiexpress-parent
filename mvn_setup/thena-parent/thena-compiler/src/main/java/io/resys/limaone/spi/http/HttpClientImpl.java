package io.resys.limaone.spi.http;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class HttpClientImpl implements HttpClient {
  
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public HttpQuery httpQuery() {
    log.trace("Creating new HTTP query builder");
    return new HttpQueryImpl();
  }
  
  @RequiredArgsConstructor
  private class HttpQueryImpl implements HttpQuery {
    private String uriPath = "";
    
    @Override
    public HttpQuery uri(Consumer<UriBuilder> uri) {
      log.trace("Configuring URI for HTTP query");
      final var builder = new UriBuilderImpl();
      uri.accept(builder);
      this.uriPath = builder.getPath();
      log.debug("HTTP query URI configured: {}", uriPath);
      return this;
    }
    
    @Override
    public <T> HttpMethodBuider<T> method(Class<T> type) {
      log.trace("Setting response type for HTTP query: {}", type.getSimpleName());
      return new HttpMethodBuilderImpl<>(uriPath, type);
    }
  }
  
  private class UriBuilderImpl implements UriBuilder {
    private final StringBuilder pathBuilder = new StringBuilder("");
    
    @Override
    public UriBuilder append(String uriFragment) {
      if (!pathBuilder.toString().endsWith("/")) {
        pathBuilder.append("/");
      }
      if (uriFragment.startsWith("/")) {
        uriFragment = uriFragment.substring(1);
      }
      pathBuilder.append(uriFragment);
      log.trace("Appending URI fragment: {} -> current path: {}", uriFragment, pathBuilder.toString());
      return this;
    }
    
    @Override
    public void build() {
      log.trace("URI building completed: {}", pathBuilder.toString());
    }
    
    public String getPath() {
      return pathBuilder.toString();
    }
  }
  
  @RequiredArgsConstructor
  private class HttpMethodBuilderImpl<T> implements HttpMethodBuider<T> {
    private final String uriPath;
    private final Class<T> responseType;
    
    @Override
    public Uni<T> postOneObject(T object) {
      log.debug("Executing POST request to: {} with object type: {}", uriPath, responseType.getSimpleName());
      return Uni.createFrom().item(() -> {
        try {
          final var headers = createJsonHeaders();
          final var body = objectMapper.writeValueAsString(object);
          log.trace("POST request body: {}", body);
          
          final var entity = new HttpEntity<>(body, headers);
          final var response = restTemplate.exchange(uriPath, HttpMethod.POST, entity, String.class);
          
          if(response.getStatusCode().is2xxSuccessful()) {
            log.debug("POST request successful, status: {}", response.getStatusCode());
            log.trace("POST response body: {}", response.getBody());
            
          } else {
            log.error("POST request unsuccessful, status: {}", response.getStatusCode());
            log.error("POST response body: {}", response.getBody());
            throw new HttpCodeException("Expected 2xx but was: " + response.getStatusCode());
          }
          
          return objectMapper.readValue(response.getBody(), responseType);
        } catch (JsonProcessingException e) {
          log.error("Failed to serialize/deserialize JSON for POST request to: {}", uriPath, e);
          throw new RuntimeException("JSON processing failed: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
          log.error("Server error during POST request to: {}, status: {}", uriPath, e.getStatusCode(), e);
          throw new RuntimeException("Server error: " + e.getMessage(), e);
        } catch (HttpClientErrorException e) {
          log.error("Client error during POST request to: {}, status: {}", uriPath, e.getStatusCode(), e);
          throw new RuntimeException("Client error: " + e.getMessage(), e);
        }
      });
    }
    
    @Override
    public <K> Uni<K> postOneObject(T object, Function<JsonObject, K> resultMapper) {
      log.debug("Executing POST request to: {} with object type: {}", uriPath, responseType.getSimpleName());
      return Uni.createFrom().item(() -> {
        try {
          final var headers = createJsonHeaders();
          final var body = objectMapper.writeValueAsString(object);
          log.trace("POST request body: {}", body);
          
          final var entity = new HttpEntity<>(body, headers);
          final var response = restTemplate.exchange(uriPath, HttpMethod.POST, entity, String.class);
          
          if(response.getStatusCode().is2xxSuccessful()) {
            log.debug("POST request successful, status: {}", response.getStatusCode());
            log.trace("POST response body: {}", response.getBody());
            
          } else {
            log.error("POST request unsuccessful, status: {}", response.getStatusCode());
            log.error("POST response body: {}", response.getBody());
            throw new HttpCodeException("Expected 2xx but was: " + response.getStatusCode());
          }
          
          return resultMapper.apply(new JsonObject(response.getBody()));
        } catch (JsonProcessingException e) {
          log.error("Failed to serialize/deserialize JSON for POST request to: {}", uriPath, e);
          throw new RuntimeException("JSON processing failed: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
          log.error("Server error during POST request to: {}, status: {}", uriPath, e.getStatusCode(), e);
          throw new RuntimeException("Server error: " + e.getMessage(), e);
        } catch (HttpClientErrorException e) {
          log.error("Client error during POST request to: {}, status: {}", uriPath, e.getStatusCode(), e);
          throw new RuntimeException("Client error: " + e.getMessage(), e);
        }
      });
    }
    
    @Override
    public Uni<T> putOneObject(T object) {
      log.debug("Executing PUT request to: {} with object type: {}", uriPath, responseType.getSimpleName());
      return Uni.createFrom().item(() -> {
        try {
          final var headers = createJsonHeaders();
          final var body = objectMapper.writeValueAsString(object);
          log.trace("PUT request body: {}", body);
          
          final var entity = new HttpEntity<>(body, headers);
          final var response = restTemplate.exchange(uriPath, HttpMethod.PUT, entity, String.class);
          
          if(response.getStatusCode().is2xxSuccessful()) {
            log.debug("PUT request successful, status: {}", response.getStatusCode());
            log.trace("PUT response body: {}", response.getBody());
            
          } else {
            log.error("PUT request unsuccessful, status: {}", response.getStatusCode());
            log.error("PUT response body: {}", response.getBody());
            throw new HttpCodeException("Expected 2xx but was: " + response.getStatusCode());
          }
          
          
          return objectMapper.readValue(response.getBody(), responseType);
        } catch (JsonProcessingException e) {
          log.error("Failed to serialize/deserialize JSON for PUT request to: {}", uriPath, e);
          throw new RuntimeException("JSON processing failed: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
          log.error("Server error during PUT request to: {}, status: {}", uriPath, e.getStatusCode(), e);
          throw new RuntimeException("Server error: " + e.getMessage(), e);
        } catch (HttpClientErrorException e) {
          log.error("Client error during PUT request to: {}, status: {}", uriPath, e.getStatusCode(), e);
          throw new RuntimeException("Client error: " + e.getMessage(), e);
        }
      });
    }
    @Override
    public <K> Uni<K> putOneObject(T object, Function<JsonObject, K> resultMapper) {
      log.debug("Executing PUT request to: {} with object type: {}", uriPath, responseType.getSimpleName());
      return Uni.createFrom().item(() -> {
        try {
          final var headers = createJsonHeaders();
          final var body = objectMapper.writeValueAsString(object);
          log.trace("PUT request body: {}", body);
          
          final var entity = new HttpEntity<>(body, headers);
          final var response = restTemplate.exchange(uriPath, HttpMethod.PUT, entity, String.class);
          
          if(response.getStatusCode().is2xxSuccessful()) {
            log.debug("PUT request successful, status: {}", response.getStatusCode());
            log.trace("PUT response body: {}", response.getBody());
            
          } else {
            log.error("PUT request unsuccessful, status: {}", response.getStatusCode());
            log.error("PUT response body: {}", response.getBody());
            throw new HttpCodeException("Expected 2xx but was: " + response.getStatusCode());
          }
          
          return resultMapper.apply(new JsonObject(response.getBody()));
        } catch (JsonProcessingException e) {
          log.error("Failed to serialize/deserialize JSON for PUT request to: {}", uriPath, e);
          throw new RuntimeException("JSON processing failed: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
          log.error("Server error during PUT request to: {}, status: {}", uriPath, e.getStatusCode(), e);
          throw new RuntimeException("Server error: " + e.getMessage(), e);
        } catch (HttpClientErrorException e) {
          log.error("Client error during PUT request to: {}, status: {}", uriPath, e.getStatusCode(), e);
          throw new RuntimeException("Client error: " + e.getMessage(), e);
        }
      });
    }
    @SuppressWarnings("unchecked")
    @Override
    public Multi<T> findAllObjects() {
      log.debug("Executing GET request for multiple objects to: {}, expecting: {}", uriPath, responseType.getSimpleName());
      return Multi.createFrom().item(() -> {
        try {
          final var headers = createJsonHeaders();
          final var entity = new HttpEntity<>(headers);
          final var response = restTemplate.exchange(uriPath, HttpMethod.GET, entity, String.class);
          
          log.debug("GET request for multiple objects successful, status: {}", response.getStatusCode());
          log.trace("GET response body: {}", response.getBody());
          
          final var array = (T[]) objectMapper.readValue(
              response.getBody(), 
              objectMapper.getTypeFactory().constructArrayType(responseType));
          
          final List<T> list = Arrays.asList(array);
          
          log.debug("Parsed {} objects from response", list.size());
          return list;
        } catch (JsonProcessingException e) {
          log.error("Failed to deserialize JSON array for GET request to: {}", uriPath, e);
          throw new RuntimeException("JSON processing failed: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
          log.error("Server error during GET request to: {}, status: {}", uriPath, e.getStatusCode(), e);
          throw new RuntimeException("Server error: " + e.getMessage(), e);
        } catch (HttpClientErrorException e) {
          log.warn("Client error during GET request to: {}, status: {} - returning empty list", uriPath, e.getStatusCode());
          return Arrays.asList();
        }
      }).onItem().disjoint();
    }
    
    @Override
    public Uni<Optional<T>> findOneObject() {
      log.debug("Executing GET request for optional object to: {}, expecting: {}", uriPath, responseType.getSimpleName());
      return Uni.createFrom().item(() -> {
        try {
          final var headers = createJsonHeaders();
          final var entity = new HttpEntity<>(headers);
          final var response = restTemplate.exchange(uriPath, HttpMethod.GET, entity, String.class);
          
          log.debug("GET request for optional object successful, status: {}", response.getStatusCode());
          log.trace("GET response body: {}", response.getBody());
          
          final var result = objectMapper.readValue(response.getBody(), responseType);
          log.debug("Successfully parsed optional object");
          return Optional.of(result);
        } catch (JsonProcessingException e) {
          log.error("Failed to deserialize JSON for GET request to: {}", uriPath, e);
          throw new RuntimeException("JSON processing failed: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
          log.error("Server error during GET request to: {}, status: {}", uriPath, e.getStatusCode(), e);
          throw new RuntimeException("Server error: " + e.getMessage(), e);
        } catch (HttpClientErrorException e) {
          log.info("Client error during GET request to: {}, status: {} - returning empty", uriPath, e.getStatusCode());
          return Optional.<T>empty();
        }
      });
    }
    
    @Override
    public Uni<T> getOneObject() {
      log.debug("Executing GET request for required object to: {}, expecting: {}", uriPath, responseType.getSimpleName());
      return Uni.createFrom().item(() -> {
        try {
          final var headers = createJsonHeaders();
          final var entity = new HttpEntity<>(headers);
          final var response = restTemplate.exchange(uriPath, HttpMethod.GET, entity, String.class);
          
          log.debug("GET request for required object successful, status: {}", response.getStatusCode());
          log.trace("GET response body: {}", response.getBody());
          
          final var result = objectMapper.readValue(response.getBody(), responseType);
          log.debug("Successfully parsed required object");
          return result;
        } catch (JsonProcessingException e) {
          log.error("Failed to deserialize JSON for GET request to: {}", uriPath, e);
          throw new RuntimeException("JSON processing failed: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
          log.error("Server error during GET request to: {}, status: {}", uriPath, e.getStatusCode(), e);
          throw new RuntimeException("Server error: " + e.getMessage(), e);
        } catch (HttpClientErrorException e) {
          log.error("Client error during GET request to: {}, status: {} - expected 1 but got 0", uriPath, e.getStatusCode(), e);
          throw new RuntimeException("Expected 1 object but got 0 - " + e.getMessage(), e);
        }
      });
    }
    
    private HttpHeaders createJsonHeaders() {
      final var headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
      log.trace("Created JSON headers: Content-Type=application/json, Accept=application/json");
      return headers;
    }

    @Override
    public Uni<RawResponse> postOneAsRaw(String body) {
      log.debug("Executing POST request to: {} with object type: {}", uriPath, responseType.getSimpleName());
      return Uni.createFrom().item(() -> {
        try {
          final var headers = createJsonHeaders();
          log.trace("POST request body: {}", body);
          
          final var entity = new HttpEntity<>(body, headers);
          final var response = restTemplate.exchange(uriPath, HttpMethod.POST, entity, String.class);
          
          if(response.getStatusCode().is2xxSuccessful()) {
            log.debug("POST request successful, status: {}", response.getStatusCode());
            log.trace("POST response body: {}", response.getBody());
            return new RawResponseImpl(response);
          } 
          log.error("POST request unsuccessful, status: {}", response.getStatusCode());
          log.error("POST response body: {}", response.getBody());
          return new RawResponseImpl(response);
        } catch (HttpServerErrorException e) {
          log.error("Server error during POST request to: {}, status: {}", uriPath, e.getStatusCode(), e);
          throw new RuntimeException("Server error: " + e.getMessage(), e);
        } catch (HttpClientErrorException e) {
          log.error("Client error during POST request to: {}, status: {}", uriPath, e.getStatusCode(), e);
          throw new RuntimeException("Client error: " + e.getMessage(), e);
        }
      });
    }

    @Override
    public Uni<RawResponse> getOneAsRaw() {
      log.debug("Executing GET request for required object to: {}, expecting: {}", uriPath, responseType.getSimpleName());
      return Uni.createFrom().item(() -> {
        try {
          final var headers = createJsonHeaders();
          final var entity = new HttpEntity<>(headers);
          final var response = restTemplate.exchange(uriPath, HttpMethod.GET, entity, String.class);
          
          log.debug("GET request for required object successful, status: {}", response.getStatusCode());
          log.trace("GET response body: {}", response.getBody());
          
          return new RawResponseImpl(response);
        } catch (HttpServerErrorException e) {
          log.error("Server error during GET request to: {}, status: {}", uriPath, e.getStatusCode(), e);
          throw new RuntimeException("Server error: " + e.getMessage(), e);
        } catch (HttpClientErrorException e) {
          log.error("Client error during GET request to: {}, status: {} - expected 1 but got 0", uriPath, e.getStatusCode(), e);
          throw new RuntimeException("Expected 1 object but got 0 - " + e.getMessage(), e);
        }
      });
    }
  }
  
  public class HttpCodeException extends RuntimeException {
    private static final long serialVersionUID = 4905500192836989583L;

    public HttpCodeException(String message, Throwable cause) {
      super(message, cause);
    }
    public HttpCodeException(String message) {
      super(message);
    }
  }

  
  public record RawResponseImpl(ResponseEntity<String> response) implements RawResponse {
    @Override
    public boolean isOk() {
      return response.getStatusCode().is2xxSuccessful();
    }
    @Override
    public String getBody() {
      return response.getBody();
    }

    @Override
    public Object unwrap() {
      return response;
    }}
}