/*
 * Copyright © 2015 - 2021 ReSys (info@dialob.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dialob.executor.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

import io.dialob.spi.Constants;

public class IdUtils {

  public static final ItemId QUESTIONNAIRE_ID = ImmutableItemRef.of(Constants.QUESTIONNAIRE, Optional.empty());

  public static String toString(ValueSetId valueSetId) {
    if (valueSetId == null) {
      return null;
    }
    return valueSetId.getValueSetId();
  }

  public static String toString(ItemId itemId) {
    if (itemId == null) {
      return null;
    }
    List<String> idChain = Lists.newArrayList();
    Optional<ItemId> id = Optional.of(itemId);
    while (id.isPresent()) {
      itemId = id.get();
      if (itemId instanceof ErrorId) {
        return toString(((ErrorId) itemId).getItemId()) + ":" + ((ErrorId) itemId).getCode();
      } else if (itemId instanceof ItemRef) {
        idChain.add(((ItemRef) itemId).getId());
      } else if (itemId instanceof ItemIndex) {
        idChain.add(((ItemIndex) itemId).getIndex().toString());
      } else if (itemId instanceof ItemIdPartial) {
        idChain.add("*");
      }
      id = itemId.getParent();
    }
    return StringUtils.join(Lists.reverse(idChain),".");
  }

  @Nullable
  public static ItemId toIdNullable(@Nullable String itemId) {
    if (itemId == null) {
      return null;
    }
    return toId(itemId);
  }



  @Nonnull
  public static ItemId toId(@Nullable String scopeId, @Nonnull String itemId) {
    if (StringUtils.isNotBlank(scopeId)) {
      return toId(scopeId + "." + itemId);
    }
    return toId(itemId);
  }

  @Nonnull
  public static ItemId toId(@Nonnull String itemId) {
    if (Constants.QUESTIONNAIRE.equals(itemId)) {
      return QUESTIONNAIRE_ID;
    }
    String[] strings = itemId.split("\\.");
    ItemId id = null;
    for (String s : strings) {
      if (StringUtils.isNumeric(s)) {
        id = ImmutableItemIndex.of(Integer.parseInt(s), Optional.ofNullable(id));
      } else if ("*".equals(s)) {
        id = ImmutableItemIdPartial.of(Optional.ofNullable(id));
      } else {
        id = ImmutableItemRef.of(s, Optional.ofNullable(id));
      }
    }
    if (id == null) {
      throw new IllegalArgumentException(itemId + " is not valid id");
    }
    return id;
  }

  public static Collection<ItemId> toIds(Iterable<String> itemIds) {
    List<ItemId> itemIdList = new ArrayList<>();
    for (String id : itemIds) {
      itemIdList.add(toId(id));
    }
    return itemIdList;
  }

  public static ItemId withIndex(ItemId itemId, int index) {
    // TODO Check
    return itemId.withParent(ImmutableItemIndex.of(index, itemId.getParent().flatMap(ItemId::getParent)));
  }

  public static boolean matches(@Nonnull Optional<ItemId> itemIdLh, @Nonnull Optional<ItemId> itemIdRh) {
    if (itemIdLh.isPresent() == itemIdRh.isPresent()) {
      return itemIdLh.map(itemId -> matches(itemId, itemIdRh.get())).orElse(true);
    }
    return false;
  }


  public static boolean matches(@Nonnull ItemId itemIdLh, @Nonnull ItemId itemIdRh) {
    if (itemIdLh.equals(itemIdRh)) {
      return true;
    }
    if (Objects.equals(itemIdLh.getValue(), itemIdRh.getValue())
      || itemIdLh instanceof ItemIdPartial || itemIdRh instanceof ItemIdPartial) {
      return matches(itemIdLh.getParent(), itemIdRh.getParent());
    }
    return false;
  }

  public static void writeIdTo(@Nullable ItemId id, CodedOutputStream output) throws IOException {
    if (id == null) {
      output.writeBoolNoTag(false);
      return;
    }
    output.writeBoolNoTag(true);
    if (id instanceof ItemRef) {
      ItemRef itemRef = (ItemRef) id;
      output.write((byte) 1);
      output.writeStringNoTag(itemRef.getValue());
    } else if (id instanceof ItemIdPartial) {
      output.write((byte) 2);
    } else if (id instanceof ItemIndex) {
      output.write((byte) 3);
      ItemIndex itemRef = (ItemIndex) id;
      output.writeInt32NoTag(itemRef.getIndex());
    } else {
      throw new RuntimeException("unknown id type " + id);
    }
    writeIdTo(id.getParent().orElse(null), output);
  }

  @Nullable
  public static ItemId readIdFrom(CodedInputStream input) throws IOException {
    if (input.readBool()) {
      byte type = input.readRawByte();
      switch (type) {
        case 1:
          return ImmutableItemRef.of(input.readString(), Optional.ofNullable(readIdFrom(input)));
        case 2:
          return ImmutableItemIdPartial.of(Optional.ofNullable(readIdFrom(input)));
        case 3:
          return ImmutableItemIndex.of(input.readInt32(), Optional.ofNullable(readIdFrom(input)));
        default:
          throw new RuntimeException("unknown id type " + type);
      }
    }
    return null;
  }
}
