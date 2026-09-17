package io.digiexpress.eveli.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "eveli.attachment.fs")
@Data
public class EveliPropsAttachmentFs {
    Boolean enabled = false;
    /**
     * If root directory is not absolute then it's used as subdirectory in temporary folder.
     */
    String rootDirectory = "attachments";
    String attachmentUrlBase = "/worker/rest/api/attachments/fs";
    String attachmentServer = "http://localhost:8080";
}
