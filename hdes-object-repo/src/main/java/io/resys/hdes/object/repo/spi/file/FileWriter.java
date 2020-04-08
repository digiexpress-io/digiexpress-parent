package io.resys.hdes.object.repo.spi.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class FileWriter implements Writer<File> {
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
    
    Map<String, Head> heads = new HashMap<>(src.getHeads());
    Map<String, Tag> tags = new HashMap<>(src.getTags());
    Map<String, IsObject> values = new HashMap<>(src.getValues());
    
    for (Object value : objects) {
      if (value instanceof Blob) {
        Blob blob = (Blob) value;
        File target = objects(blob);
        values.put(blob.getId(), visitBlob(target, blob));
      
      } else if (value instanceof Commit) {
        Commit commit = (Commit) value;
        File target = objects(commit);
        values.put(commit.getId(), visitCommit(target, commit));
      
      } else if (value instanceof Tree) {
        Tree tree = (Tree) value;
        File target = objects(tree);
        values.put(tree.getId(), visitTree(target, tree));
      
      } else if (value instanceof Head) {
        Head head = (Head) value;
        File target = new File(config.getHeads(), head.getName());
        heads.put(head.getName(), visitHead(target, head));
      
      } else if (value instanceof Tag) {
        Tag tag = (Tag) value;
        File target = new File(config.getTags(), tag.getName());
        tags.put(tag.getName(), visitTag(target, tag));

      } else {
        throw new RepoException("Unknown object: " + value);
      }
    }
    LOGGER.debug(log.toString());
    return ImmutableObjects.builder().values(values).heads(heads).tags(tags).build();
  }

  @Override
  public Head visitHead(File target, Head head) {
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
  public Tag visitTag(File target, Tag tag) {
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
  public Commit visitCommit(File target, Commit commit) {
    
    if (target.exists()) {
      log.append("  - commit reuse: ").append(target.getPath()).append(System.lineSeparator());
      return commit;
    }
    try {
      FileUtils.mkFile(target);
      
      FileOutputStream fileOutputStream = new FileOutputStream(target);
      IOUtils.copy(new ByteArrayInputStream(serializer.visitObject(commit)), fileOutputStream);
      log.append("  - commit: ").append(target.getPath()).append(System.lineSeparator());
      return commit;
    } catch (IOException e) {
      throw new RepoException("Failed to write OBJECT file into " + target.getName() + " because: " + e.getMessage() + "!", e);
    }
  }

  @Override
  public Blob visitBlob(File target, Blob blob) {
    if (target.exists()) {
      log.append("  - blob reuse: ").append(target.getPath()).append(System.lineSeparator());
      return blob;
    }
    try {
      FileUtils.mkFile(target);
      
      FileOutputStream fileOutputStream = new FileOutputStream(target);
      IOUtils.copy(new ByteArrayInputStream(serializer.visitObject(blob)), fileOutputStream);
      log.append("  - blob: ").append(target.getPath()).append(System.lineSeparator());
      return blob;
    } catch (IOException e) {
      throw new RepoException("Failed to write OBJECT file into " + target.getPath() + " because: " + e.getMessage() + "!", e);
    }
  }

  @Override
  public Tree visitTree(File target, Tree tree) {
    if (target.exists()) {
      log.append("  - tree reuse: ").append(target.getPath()).append(System.lineSeparator());
      return tree;
    }
    
    try {
      FileUtils.mkFile(target);
      
      FileOutputStream fileOutputStream = new FileOutputStream(target);
      IOUtils.copy(new ByteArrayInputStream(serializer.visitObject(tree)), fileOutputStream);
      log.append("  - tree: ").append(target.getPath()).append(System.lineSeparator());
      return tree;
    } catch (IOException e) {
      throw new RepoException("Failed to write OBJECT file into " + target.getPath() + " because: " + e.getMessage() + "!", e);
    }
  }

  private File objects(IsObject object) {
    File directory = new File(config.getObjects(), object.getId().substring(0, 2));
    FileUtils.mkdir(directory);
    return new File(directory, object.getId().substring(2));
  }
}
