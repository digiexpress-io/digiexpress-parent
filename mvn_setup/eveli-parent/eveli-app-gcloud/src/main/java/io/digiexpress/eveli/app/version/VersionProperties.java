package io.digiexpress.eveli.app.version;

import java.io.IOException;
import java.util.Properties;

public final class VersionProperties {
  public static final String TAGS;
  public static final String BRANCH;
  public static final String DIRTY;
  public static final String REMOTE_ORIGIN_URL;

  public static final String COMMIT_ID;
  public static final String COMMIT_ID_ABBREV;
  public static final String DESCRIBE;
  public static final String DESCRIBE_SHORT;
  public static final String COMMIT_USER_NAME;
  public static final String COMMIT_USER_EMAIL;
  public static final String COMMIT_MESSAGE_FULL;
  public static final String COMMIT_MESSAGE_SHORT;
  public static final String COMMIT_TIME;
  public static final String CLOSEST_TAG_NAME;
  public static final String CLOSEST_TAG_COMMIT_COUNT;

  public static final String BUILD_USER_NAME;
  public static final String BUILD_USER_EMAIL;
  public static final String BUILD_TIME;
  public static final String BUILD_HOST;
  public static final String BUILD_VERSION;
  public static final String BUILD_NUMBER;
  public static final String BUILD_NUMBER_UNIQUE;

  static {
    try {
      Properties properties = new Properties();
      properties.load(VersionProperties.class.getClassLoader().getResourceAsStream("git.properties"));

      TAGS = String.valueOf(properties.get("git.tags"));
      BRANCH = String.valueOf(properties.get("git.branch"));
      DIRTY = String.valueOf(properties.get("git.dirty"));
      REMOTE_ORIGIN_URL = String.valueOf(properties.get("git.remote.origin.url"));

      COMMIT_ID = String.valueOf(properties.get("git.commit.id")); 
      COMMIT_ID_ABBREV = String.valueOf(properties.get("git.commit.id.abbrev"));
      DESCRIBE = String.valueOf(properties.get("git.commit.id.describe"));
      DESCRIBE_SHORT = String.valueOf(properties.get("git.commit.id.describe-short"));
      COMMIT_USER_NAME = String.valueOf(properties.get("git.commit.user.name"));
      COMMIT_USER_EMAIL = String.valueOf(properties.get("git.commit.user.email"));
      COMMIT_MESSAGE_FULL = String.valueOf(properties.get("git.commit.message.full"));
      COMMIT_MESSAGE_SHORT = String.valueOf(properties.get("git.commit.message.short"));
      COMMIT_TIME = String.valueOf(properties.get("git.commit.time"));
      CLOSEST_TAG_NAME = String.valueOf(properties.get("git.closest.tag.name"));
      CLOSEST_TAG_COMMIT_COUNT = String.valueOf(properties.get("git.closest.tag.commit.count"));

      BUILD_USER_NAME = String.valueOf(properties.get("git.build.user.name"));
      BUILD_USER_EMAIL = String.valueOf(properties.get("git.build.user.email"));
      BUILD_TIME = String.valueOf(properties.get("git.build.time"));
      BUILD_HOST = String.valueOf(properties.get("git.build.host"));
      BUILD_VERSION = String.valueOf(properties.get("git.build.version"));
      BUILD_NUMBER = String.valueOf(properties.get("git.build.number"));
      BUILD_NUMBER_UNIQUE = String.valueOf(properties.get("git.build.number.unique"));
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private VersionProperties() {}
}