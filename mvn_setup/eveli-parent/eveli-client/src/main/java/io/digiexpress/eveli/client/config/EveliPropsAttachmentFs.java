package io.digiexpress.eveli.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "eveli.attachment.fs")
@Data
public class EveliPropsAttachmentFs {
    Boolean enabled = false;
    String rootDirectory = "attachments";
    String attachmentUrlBase = "/worker/rest/api/attachments/fs";
    String attachmentServer = "http://localhost:8080";
}
