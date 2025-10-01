package io.digiexpress.tagomi.api;

import io.digiexpress.tagomi.api.commands.TagomiCreateCommands;
import io.digiexpress.tagomi.api.commands.TagomiDeleteCommands;
import io.digiexpress.tagomi.api.commands.TagomiUpdateCommands;



public interface TagomiComposer {
  TagomiCreateCommands create();
  TagomiUpdateCommands update();
  TagomiDeleteCommands delete();

}
