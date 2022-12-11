package io.digiexpress.client.spi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;

import io.dialob.api.form.Form;
import io.digiexpress.client.api.Archiver;
import io.digiexpress.client.api.ImmutableCompressed;
import io.digiexpress.client.api.ClientEntity.ServiceDefinition;
import io.digiexpress.client.api.ClientEntity.ServiceRelease;
import io.digiexpress.client.api.Parser;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.spi.staticresources.Sha2;
import io.thestencil.client.api.MigrationBuilder.Sites;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ArchiverImpl implements Archiver {
  private static Charset UTF_8 = StandardCharsets.UTF_8;
  private final Parser mapper;
  
  @Override
  public Compressed compress(AstTag hdes) {
    return writeGZip(mapper.toRelease(hdes));
  }
  @Override
  public Compressed compress(Sites stencil) {
    return writeGZip(mapper.toRelease(stencil));
  }
  @Override
  public Compressed compress(Form form) {
    return writeGZip(mapper.toRelease(form));
  }
  @Override
  public Compressed compress(ServiceDefinition def) {
    return writeGZip(mapper.toRelease(def));
  }
  @Override
  public Compressed compress(ServiceRelease rel) {
    return writeGZip(mapper.toRelease(rel));
  }
  @Override
  public AstTag decompressionHdes(String body) {
    final var resp = readGzip(new ByteArrayInputStream(body.getBytes(UTF_8)));
    return mapper.toHdes(resp);
  }
  @Override
  public Sites decompressionStencil(String body) {
    final var resp = readGzip(new ByteArrayInputStream(body.getBytes(UTF_8)));
    return mapper.toStencil(resp);
  }
  @Override
  public Form decompressionDialob(String body) {
    final var resp = readGzip(new ByteArrayInputStream(body.getBytes(UTF_8)));
    return mapper.toDialob(resp);
  }
  @Override
  public ServiceDefinition decompressionService(String body) {
    final var resp = readGzip(new ByteArrayInputStream(body.getBytes(UTF_8)));
    return mapper.toDefinition(resp);
  }
  @Override
  public ServiceRelease decompressionRelease(String body) {
    final var resp = readGzip(new ByteArrayInputStream(body.getBytes(UTF_8)));
    return mapper.toRelease(resp);
  }
  public Compressed writeGZip(String json) {
    try {        
      final var hash = Sha2.blob(json);      
      final var toCompress = json.getBytes(UTF_8);        
      final var byteArray = new ByteArrayOutputStream();
      final var b64os = new Base64OutputStream(byteArray);

      final var zipStream = new GZIPOutputStream(b64os);
      zipStream.write(toCompress);
      zipStream.close();
      
      b64os.close();
      byteArray.close();
       
      return ImmutableCompressed.builder()
          .hash(hash)
          .value(new String(byteArray.toByteArray(), UTF_8))
          .build();
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  public String readGzip(InputStream input) {
    try {
      final var content = new String(input.readAllBytes(), UTF_8); 
      final var b64os = new Base64InputStream(new ByteArrayInputStream(content.getBytes(UTF_8)));
      final var gzip = new GZIPInputStream(b64os);
      final var doc = new String(gzip.readAllBytes(), UTF_8);
      return doc;
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
