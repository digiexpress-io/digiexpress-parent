package io.resys.hdes.object.repo.spi.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.resys.hdes.object.repo.api.ImmutableObjects;
import io.resys.hdes.object.repo.api.ObjectRepository.Blob;
import io.resys.hdes.object.repo.api.ObjectRepository.Commit;
import io.resys.hdes.object.repo.api.ObjectRepository.Head;
import io.resys.hdes.object.repo.api.ObjectRepository.IsObject;
import io.resys.hdes.object.repo.api.ObjectRepository.Objects;
import io.resys.hdes.object.repo.api.ObjectRepository.Tag;
import io.resys.hdes.object.repo.api.ObjectRepository.Tree;
import io.resys.hdes.object.repo.api.exceptions.RepoException;
import io.resys.hdes.object.repo.spi.ObjectRepositoryMapper.Serializer;
import io.resys.hdes.object.repo.spi.ObjectRepositoryMapper.Writer;
import io.resys.hdes.object.repo.spi.file.util.FileUtils;
import io.resys.hdes.object.repo.spi.file.util.FileUtils.FileSystemConfig;

public class FileWriter implements Writer {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileWriter.class);
  
  private final FileSystemConfig config;
  private final Serializer serializer;
  private final Objects src;
  private final StringBuilder log = new StringBuilder("Writing transaction: ").append(System.lineSeparator());

  public FileWriter(Objects src, FileSystemConfig config, Serializer serializer) {
    super();
    this.config = config;
    this.src = src;
    this.serializer = serializer;
  }

  @Override
  public Objects build(List<Object> objects) {
    ImmutableObjects.Builder builder = ImmutableObjects.builder().from(src);
    for (Object value : objects) {
      if (value instanceof Blob) {
        builder.putValues(((Blob) value).getId(), visitBlob((Blob) value));
      } else if (value instanceof Commit) {
        builder.putValues(((Commit) value).getId(), visitCommit((Commit) value));
      } else if (value instanceof Tree) {
        builder.putValues(((Tree) value).getId(), visitTree((Tree) value));
      } else if (value instanceof Head) {
        builder.putHeads(((Head) value).getName(), visitHead((Head) value));
      } else if (value instanceof Tag) {
        builder.putTags(((Tag) value).getName(), visitTag((Tag) value));
      } else {
        throw new RepoException("Unknown object: " + value);
      }
    }
    
    LOGGER.debug(log.toString());
    Objects result = builder.build();
    return result;
  }

  @Override
  public Head visitHead(Head head) {
    File target = new File(config.getHeads(), head.getName());
    try {
      target = FileUtils.mkFile(target);
      FileOutputStream fileOutputStream = new FileOutputStream(target);
      IOUtils.copy(new ByteArrayInputStream(serializer.visitHead(head)), fileOutputStream);
      
      log.append("  - ").append(head).append(System.lineSeparator());
      return head;
    } catch (IOException e) {
      throw new RepoException("Failed to write HEAD file into " + target.getName() + " because: " + e.getMessage() + "!", e);
    }
  }

  @Override
  public Tag visitTag(Tag tag) {
    File target = new File(config.getTags(), tag.getName());
    try {
      target = FileUtils.mkFile(target);
      FileOutputStream fileOutputStream = new FileOutputStream(target);
      IOUtils.copy(new ByteArrayInputStream(serializer.visitTag(tag)), fileOutputStream);
      log.append("  - ").append(tag).append(System.lineSeparator());
      return tag;
    } catch (IOException e) {
      throw new RepoException("Failed to write HEAD file into " + target.getPath() + " because: " + e.getMessage() + "!", e);
    }
  }

  @Override
  public Commit visitCommit(Commit commit) {
    File target = objects(commit);
    if (target.exists()) {
      log.append("  - commit reuse: ").append(target.getPath()).append(System.lineSeparator());
      return commit;
    }
    try {
      FileOutputStream fileOutputStream = new FileOutputStream(target);
      IOUtils.copy(new ByteArrayInputStream(serializer.visitObject(commit)), fileOutputStream);
      log.append("  - commit: ").append(target.getPath()).append(System.lineSeparator());
      return commit;
    } catch (IOException e) {
      throw new RepoException("Failed to write OBJECT file into " + target.getName() + " because: " + e.getMessage() + "!", e);
    }
  }

  @Override
  public Blob visitBlob(Blob blob) {
    File target = objects(blob);
    if (target.exists()) {
      log.append("  - blob reuse: ").append(target.getPath()).append(System.lineSeparator());
      return blob;
    }
    try {
      FileOutputStream fileOutputStream = new FileOutputStream(target);
      IOUtils.copy(new ByteArrayInputStream(serializer.visitObject(blob)), fileOutputStream);
      log.append("  - blob: ").append(target.getPath()).append(System.lineSeparator());
      return blob;
    } catch (IOException e) {
      throw new RepoException("Failed to write OBJECT file into " + target.getPath() + " because: " + e.getMessage() + "!", e);
    }
  }

  @Override
  public Tree visitTree(Tree tree) {
    File target = objects(tree);
    if (target.exists()) {
      log.append("  - tree reuse: ").append(target.getPath()).append(System.lineSeparator());
      return tree;
    }
    try {
      FileOutputStream fileOutputStream = new FileOutputStream(target);
      IOUtils.copy(new ByteArrayInputStream(serializer.visitObject(tree)), fileOutputStream);
      log.append("  - tree: ").append(target.getPath()).append(System.lineSeparator());
      return tree;
    } catch (IOException e) {
      throw new RepoException("Failed to write OBJECT file into " + target.getPath() + " because: " + e.getMessage() + "!", e);
    }
  }

  private File objects(IsObject object) {
    try {
      File directory = new File(config.getObjects(), object.getId().substring(0, 2));
      FileUtils.mkdir(directory);
      return FileUtils.mkFile(new File(directory, object.getId().substring(2)));
    } catch (IOException e) {
      throw new RepoException("Failed to write OBJECT: " + object.getId() + " because: " + e.getMessage() + "!", e);
    }
  }
}
