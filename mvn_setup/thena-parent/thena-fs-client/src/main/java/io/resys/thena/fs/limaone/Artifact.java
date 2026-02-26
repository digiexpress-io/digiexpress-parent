package io.resys.thena.fs.limaone;

public interface Artifact {
  String getObjectId();
  String getHash();
  Dependencies getDependencies();
  

  interface Dependencies {
    String getObjectId();
    String getHash();
  }
}
