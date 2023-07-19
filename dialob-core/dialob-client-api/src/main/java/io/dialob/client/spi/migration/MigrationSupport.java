package io.dialob.client.spi.migration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;

import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobDocument.FormReleaseDocument;
import io.dialob.client.spi.support.Sha2;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MigrationSupport {
  private static Charset UTF_8 = StandardCharsets.UTF_8;

  @Data
  @Builder
  public static class Migration {
    private FormReleaseDocument release;
    private String log;
    private String hash;
  }
  @Data
  @Builder
  public static class MigrationContent {
    private FormReleaseDocument release;
    private String hash;
  }  
  
  private static String BEGIN = "-----BEGIN RELEASE JSON GZIP BASE64-----\r\n";
  private static String END =   "-----END RELEASE JSON GZIP BASE64-----";
  
  private final DialobClient.TypesMapper config;


  
  public MigrationContent read(InputStream input) {
    try {
      var content = new String(input.readAllBytes(), UTF_8);
      content = content.substring(content.indexOf(BEGIN) + BEGIN.length(), content.indexOf(END)); 
      final var b64os = new Base64InputStream(new ByteArrayInputStream(content.getBytes(UTF_8)));
      final var gzip = new GZIPInputStream(b64os);
      final var doc = new String(gzip.readAllBytes(), UTF_8);
      return MigrationContent.builder()
          .release(config.readReleaseDoc(doc))
          .hash(Sha2.blob(doc))
          .build();
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  public void write(Migration migration, OutputStream output) {
    try {
      try {
        final var json = config.toJson(migration.getRelease());
        final var hash = Sha2.blob(json);
        migration.setHash(hash);
        
        
        final var toCompress = json.getBytes(UTF_8);        
        final var byteArray = new ByteArrayOutputStream();
        final var b64os = new Base64OutputStream(byteArray);

        final var zipStream = new GZIPOutputStream(b64os);
        zipStream.write(toCompress);
        zipStream.close();
        
        b64os.close();
        byteArray.close();
        final var compressed = byteArray.toByteArray();
        
        output.write(migration.getLog().getBytes(UTF_8));
        output.write((System.lineSeparator() + System.lineSeparator()).getBytes(UTF_8));
        
        output.write(("Content hash: " + hash  + System.lineSeparator()).getBytes(UTF_8));
        output.write((BEGIN).getBytes(UTF_8));
        output.write(compressed);
        output.write((END + System.lineSeparator()).getBytes(UTF_8));
        output.flush();
      } finally {
        output.close();
      }
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}