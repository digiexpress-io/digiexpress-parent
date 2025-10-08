package io.digiexpress.tagomi.spi;

import io.digiexpress.tagomi.api.TagomiComposer;
import io.digiexpress.tagomi.api.TagomiImageStorage;
import io.digiexpress.tagomi.api.TagomiStore;
import io.digiexpress.tagomi.api.commands.TagomiCreateCommands;
import io.digiexpress.tagomi.api.commands.TagomiDeleteCommands;
import io.digiexpress.tagomi.api.commands.TagomiUpdateCommands;
import io.digiexpress.tagomi.spi.commands.TagomiCreateCommandsImpl;
import io.digiexpress.tagomi.spi.commands.TagomiDeleteCommandsImpl;
import io.digiexpress.tagomi.spi.commands.TagomiUpdateCommandsImpl;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TagomiComposerImpl implements TagomiComposer {
  private final TagomiStore store;
  private final TagomiImageStorage imageStorage;
  @Override
  public TagomiCreateCommands create() {
    return new TagomiCreateCommandsImpl(store, imageStorage);
  }
  @Override
  public TagomiUpdateCommands update() {
    return new TagomiUpdateCommandsImpl(store, imageStorage);
  }
  @Override
  public TagomiDeleteCommands delete() {
    return new TagomiDeleteCommandsImpl(store);
  }
}
