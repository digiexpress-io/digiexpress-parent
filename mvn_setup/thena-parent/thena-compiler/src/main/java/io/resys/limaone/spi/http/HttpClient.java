package io.resys.limaone.spi.http;

import java.util.Optional;
import java.util.function.Consumer;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

/**
 * Reactive HTTP client interface for querying internal LimaOne dependencies and services.
 * 
 * <p>This is a wrapper around Spring's HTTP client (RestTemplate) providing a fluent, 
 * reactive API for making HTTP requests to internal services. Designed specifically 
 * for integration with local dependencies that exist elsewhere in the system.
 * 
 * <p>All operations work with JSON payloads and provide type-safe deserialization.
 * 
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * httpClient.httpQuery()
 *   .uri(uri -> uri.append("api").append("users"))
 *   .method(User.class)
 *   .findAllObjects()
 *   .subscribe().with(users -> processUsers(users));
 * }</pre>
 * 
 * <h3>Error Handling:</h3>
 * <ul>
 *   <li>4xx HTTP status codes are treated as "not found" scenarios (0 results)</li>
 *   <li>5xx HTTP status codes cause failures and exceptions</li>
 * </ul>
 * 
 * @since 1.0
 */
public interface HttpClient {

  /**
   * Creates a new HTTP query builder for constructing and executing HTTP requests.
   * 
   * @return a new {@link HttpQuery} instance for building requests
   */
  HttpQuery httpQuery();
  
  /**
   * Builder interface for constructing HTTP queries with URI and type information.
   * Provides a fluent API for configuring the target URL and response type.
   */
  interface HttpQuery {
    /**
     * Configures the target URI for the HTTP request using a builder pattern.
     * The base URI is automatically configured and path fragments are joined with slashes.
     * 
     * @param uri consumer that configures the URI using {@link UriBuilder}
     * @return this query builder for method chaining
     */
    HttpQuery uri(Consumer<UriBuilder> uri);
    
    /**
     * Specifies the expected response type for JSON deserialization and returns 
     * a method builder for executing HTTP operations.
     * 
     * @param <T> the type of objects to work with
     * @param type the class representing the response type for JSON deserialization
     * @return a {@link HttpMethodBuider} configured for the specified type
     */
    <T extends Object> HttpMethodBuider<T> method(Class<T> type);
  }
  
  /**
   * Builder interface providing HTTP method operations for a specific object type.
   * All methods work with JSON payloads and return reactive types (Uni/Multi).
   * 
   * @param <T> the type of objects being sent/received
   */
  interface HttpMethodBuider<T> {
    /**
     * Sends a POST request with the specified object as JSON payload.
     * 
     * @param object the object to send as JSON in the request body
     * @return a {@link Uni} containing the response object
     * @throws RuntimeException if the request fails (5xx status codes)
     */
    Uni<T> postOneObject(T object);
    
    /**
     * Sends a PUT request with the specified object as JSON payload.
     * 
     * @param object the object to send as JSON in the request body
     * @return a {@link Uni} containing the response object
     * @throws RuntimeException if the request fails (5xx status codes)
     */
    Uni<T> putOneObject(T object);
    
    /**
     * Performs a GET request expecting multiple objects in the response.
     * 
     * @return a {@link Multi} stream of response objects
     * @throws RuntimeException if the request fails (5xx status codes)
     */
    Multi<T> findAllObjects();
    
    /**
     * Performs a GET request expecting zero or one object in the response.
     * Returns empty Optional for 4xx status codes (not found scenarios).
     * 
     * @return a {@link Uni} containing an Optional that may be empty if no object is found
     * @throws RuntimeException if the request fails (5xx status codes)
     */
    Uni<Optional<T>> findOneObject();
    
    /**
     * Performs a GET request expecting exactly one object in the response.
     * Throws an exception if no object is found (expected 1 but got 0).
     * 
     * @return a {@link Uni} containing the response object
     * @throws RuntimeException if no object is found or if the request fails (4xx/5xx status codes)
     */
    Uni<T> getOneObject();
  }
  
  /**
   * Builder interface for constructing URIs with automatic path fragment joining.
   * Handles base URI configuration and automatic slash insertion between fragments.
   */
  interface UriBuilder {
    /**
     * Appends a path fragment to the URI. Slashes are automatically added between fragments.
     * 
     * @param uriFragment the path fragment to append (without leading/trailing slashes)
     * @return this builder for method chaining
     */
    UriBuilder append(String uriFragment);
    
    /**
     * Finalizes the URI construction and prepares it for use in the HTTP request.
     * This method completes the URI building process.
     */
    void build();
  }
}
