# Introduction

Dialob is an open source platform developed to provide easy way to create, test and manage online dialogs that can be used on any online channel.

Dialob consists of following main modules:
* **Dialob Composer** that is a tool for subject matter experts, SMEs, to create and test online dialogs. Dialob Composer is available as a cloud service.
* **Dialob Manager** that is a backend service that is integrated to given online channel(s) and executes the dialog defined by Dialob Composer

This documentation provides information how a Dialob Manager can be integrated to a given online channel. Go [Dialob](www.dialob.io) to see some live examples.

# This repository

This repository is a Java API for Dialob.
Technical documentation about APIs and protocols is available [here](https://dialob.github.io/).

# Using client

```java
ApiClient apiClient = new ApiClient("ApiKey", "<secret-api-key>");
DialobFormServiceApi formService = apiClient.buildClient(DialobFormServiceApi.class);
DialobQuestionnaireServiceApi questionnaireService = apiClient.buildClient(DialobQuestionnaireServiceApi.class);
```
