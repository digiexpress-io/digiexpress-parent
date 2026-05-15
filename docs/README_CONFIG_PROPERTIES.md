# About

This document contains list of configuration properties and describes their usage.



# Standard Spring Boot configuration properties

## Database connection properties

* `spring.datasource.url` - JDBC URL of the database. Database should be PostgreSQL, version 16.
* `spring.datasource.username` - Login username of the database.
* `spring.datasource.password` - Login password of the database.

## Active profiles

* `spring.profiles.active` - this depends from actual eveli application, Digiexpress example application uses `jwt` profile to use JWT tokens from gateways to get user and authorization information. 

## Logging configuration

* `logging.config` - location for file defining logback logging configuration.
* `logging.level.*` - changing.logging level of specific loggers (in specific classes)


# Eveli configuration properties

Here are given properties which depend from environment and require customization. For full list of configuration properties information see classes in package `io.digiexpress.eveli.client.config`

## Endpoints to other services

* `eveli.dialob.service-url`: URL of dialob service, for both API and session. This assumes that there is some proxy or load balancer, which handles requests to this endpoint and routes them between dialob API and dialob session service. Actual url-s to services are obtained by adding assumed `dialob/api` and `session/dialob` paths to this service URL.
* `eveli.dialob.api-url`, `eveli.dialob.session-url`: optional API and session URL if they are in in different addresses, overriding `eveli.dialob.service-url`.

* `eveli.crm.host`: URL for portal gateway, which services client (person or company roles). Current implementation uses Suomi.fi authorization service to obtain authorization roles. 

* `eveli.org.service-url`: URL for organization service, providing mapping from group names to group member email addresses. Typically it is eveli gateway, providing this service.

* `eveli.printout.service-url`: URL for printout service, providing PDF for dialob forms. This service expects input JSON in following format:
``` 
{"lang":"en", "form":{...dialob form...}, "session":{...dialob session...}}
```
* `eveli.feedback.analyzer.endpoint-url`: feedback analyzer URL for providing sentiment of feedback. This is based on text analyze of feedback.

* `eveli.attachment-config.download-bucket`: bucket name in cloud environment to store task attachments. Digiexpress contains example implementation for Google Cloud.

* `eveli.tagomi.service-url`: service URL for next generation PDF generator. This uses custom templates created in Digiexpress.

## Security properties

* `eveli.jwt.eveli-public-key-value`, `eveli.jwt.eveli-issuer`: RSA public key and issuer for validating backend user tokens. These are used to validate JWT tokens from backend gateway.

* `eveli.jwt.gamut-public-key-value`, `eveli.jwt.gamut-issuer`: RSA public key and issuer for validating portal user tokens.

## Suomi.fi notification service properties
* `eveli.suomifi.rest.enabled`: boolean value to enable/disable Suomi.fi notification service  
* `eveli.suomifi.rest.service-id`: service client ID 
* `eveli.suomifi.rest.password`:  service client password
* `eveli.suomifi.rest.endpoint`: endpoint for Suomi.fi service

## Email service properties
* `eveli.email.enabled`: boolean to enable/disable email sending. 
* `eveli.email.host-name`: email server's host name 
* `eveli.email.host-port`: email server's port
* `eveli.email.sender-email`: sender email address
* `eveli.email.sender-name`: name of sender
* `eveli.email.server-user-name`: email server login user
* `eveli.email.server-password`: email server login password
* `eveli.email.allowed-recipients`: list of specific users to whom email sending is allowed
* `eveli.email.enabled-domains`: list of specific email address domain to whom email sending is allowed

## Service properties

* `eveli.feedback.enabled` - boolean flag to enable feedback functionality. 


## Backend configuration properties

* `eveli.envir.dev-enabled` - "true" to show assets with dev mode enabled in portal.
* `eveli.tenant-features`- comma-separated list of UI and backend features. 
List of features:
- wrench-disabled : wrench UI is not available, usable in production environments where wrench uses fixed version.
- stencil-disabled: stencil UI is available, usable in production environment where stencil uses fixed version. 
- external-deployment
- queues-visually-disabled: queues UI is not available
- batches: batches are enabled
- batches-dev: development batches are enabled