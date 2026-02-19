package io.resys.thena.fs.spi.committree;

import java.util.Collection;

import lombok.Value;

@Value
public class CommitTreeBlobsAndProps {
  Collection<String> blobs;
  Collection<String> props;
}