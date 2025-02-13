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

import static io.dialob.compiler.Utils.readNullableString;
import static io.dialob.compiler.Utils.writeNullableString;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

import io.dialob.api.proto.Action;
import io.dialob.compiler.Utils;
import io.dialob.rule.parser.api.PrimitiveValueType;
import io.dialob.rule.parser.api.ValueType;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@EqualsAndHashCode
@ToString
public class ItemState implements SessionObject {

  private static final long serialVersionUID = -3974128908954128671L;


  public enum Status {
    /**
     * Item state instance is just created, but have not been evaluated
     */
    NEW,

    /**
     * Normal state
     */
    OK,

    /**
     * Update failed
     */
    ERROR,

    /**
     * expecting value from asyncronous evaluation
     */
    PENDING
  }

  private static final int DISPLAY_ITEM_BIT = 1 << 0;
  private static final int ACTIVE_BIT = 1 << 1;
  private static final int DISABLED_BIT = 1 << 2;
  private static final int REQUIRED_BIT = 1 << 3;
  private static final int ROWS_CAN_BE_ADDED_BIT = 1 << 4;
  private static final int ROW_CAN_BE_REMOVED_BIT = 1 << 5;
  private static final int INVALID_ANSWERS_BIT = 1 << 6;
  private static final int HAS_CUSTOM_PROPS_BIT = 1 << 7;


  private final ItemId id;

  private final ItemId prototypeId;

  private final String type;

  private final String view;

  private final String valueSetId;

  private Status status = Status.NEW;

  private Object answer;

  private Object value;

  private Object defaultValue;

  private int bits = (ACTIVE_BIT | ROWS_CAN_BE_ADDED_BIT);

  private String label;

  private String description;

  // indicates whethet questionnaire is completed

  private List<String> classNames = ImmutableList.of();

  private List<ItemId> items = ImmutableList.of();

  private List<ItemId> availableItems = ImmutableList.of();

  private Map<String, Object> props = new HashMap<>();

  private Set<Action.Type> allowedActions = ImmutableSet.of();

  private ItemId activePage;

  protected void setBits(boolean toValue, int bit) {
    if (toValue) {
      setBits(bit);
    } else {
      resetBits(bit);
    }
  }
  protected void setBits(int bit) {
    bits = bits | bit;
  }


  protected void resetBits(int bit) {
    bits = bits & (~bit);
  }

  protected boolean isBit(int bit) {
    return (bits & bit) != 0;
  }

  public static ItemState readFrom(CodedInputStream input) throws IOException {
    final ItemId id = IdUtils.readIdFrom(input);
    final ItemId prototypeId = IdUtils.readIdFrom(input);
    final String type = input.readString();
    final String view = readNullableString(input);
    final String valueSetId = readNullableString(input);

    ItemState state = new ItemState(id, prototypeId, type, view, valueSetId);
    state.activePage = IdUtils.readIdFrom(input);
    state.status = Status.values()[input.readRawByte()];
    state.bits = input.readInt32();
    state.label = readNullableString(input);
    state.description = readNullableString(input);

    state.answer = Utils.readObjectValue(input);
    state.value = readValue(input);
    state.defaultValue = readValue(input);

    state.classNames = readStringList(input);
    state.items = readIdList(input);
    state.availableItems = readIdList(input);

    int count = input.readInt32();
    if ( count >  0) {
      Action.Type[] types = new Action.Type[count];
      for (int i = 0; i < count; i++ ){
        types[i] = Action.Type.values()[input.readInt32()];
      }
      state.allowedActions = ImmutableSet.copyOf(types);
    } else {
      state.allowedActions = ImmutableSet.of();
    }
    return state;
  }


  public void writeTo(CodedOutputStream output) throws IOException {
    IdUtils.writeIdTo(id, output);
    IdUtils.writeIdTo(prototypeId, output);
    output.writeStringNoTag(type);
    writeNullableString(output, view);
    writeNullableString(output, valueSetId);

    IdUtils.writeIdTo(activePage, output);
    output.writeRawByte(status.ordinal());
    output.writeInt32NoTag(bits);
    writeNullableString(output, label);
    writeNullableString(output, description);

    Utils.writeObjectValue(output, answer);
    writeValue(output, Utils.mapQuestionTypeToValueType(type).orElse(null), value);
    writeValue(output, Utils.mapQuestionTypeToValueType(type).orElse(null), defaultValue);

    writeStringList(output, classNames);
    writeIdList(output, items);
    writeIdList(output, availableItems);

    output.writeInt32NoTag(allowedActions.size());
    for (Action.Type actionType: allowedActions) {
      output.writeInt32NoTag(actionType.ordinal());
    }
  }



  private void writeValue(CodedOutputStream output, ValueType type, Object value) throws IOException {
    final boolean present = value != null && type != null;
    output.writeBoolNoTag(present);
    if (present) {
      output.writeRawByte(type.getTypeCode());
      type.writeTo(output, value);
    }
  }

  private static Object readValue(CodedInputStream input) throws IOException {
    if (input.readBool()) {
      byte typeCode = input.readRawByte();
      ValueType valueType;
      if ((0x80 & typeCode) != 0) {
        typeCode = (byte) (typeCode & 0x7f);
        valueType = ValueType.arrayOf(PrimitiveValueType.values()[typeCode]);
      } else {
        valueType = PrimitiveValueType.values()[typeCode];
      }
      return valueType.readFrom(input);
    }
    return null;
  }


  private void writeIdList(CodedOutputStream output, List<ItemId> itemIds) throws IOException {
    output.writeInt32NoTag(itemIds.size());
    for (ItemId s : itemIds) {
      IdUtils.writeIdTo(s, output);
    }
  }

  private void writeStringList(CodedOutputStream output, List<String> stringList) throws IOException {
    output.writeInt32NoTag(stringList.size());
    for (String s : stringList) {
      output.writeStringNoTag(s);
    }
  }


  private static List<ItemId> readIdList(CodedInputStream input) throws IOException {
    int count = input.readInt32();
    if (count > 0) {
      ItemId[] ids = new ItemId[count];
      for (int i = 0; i < count; i++) {
        ids[i] = IdUtils.readIdFrom(input);
      }
      return ImmutableList.copyOf(ids);
    }
    return ImmutableList.of();
  }

  private static List<String> readStringList(CodedInputStream input) throws IOException {
    int count = input.readInt32();
    if (count > 0) {
      String[] ids = new String[count];
      for (int i = 0; i < count; i++) {
        ids[i] = input.readString();
      }
      return ImmutableList.copyOf(ids);
    }
    return ImmutableList.of();
  }



  public ItemState(@Nonnull ItemId id, ItemId prototypeId, @Nonnull String type, String view, String valueSetId) {
    this.id = id;
    this.prototypeId = prototypeId;
    this.type = type;
    this.view = view;
    this.valueSetId = valueSetId;
    resetBits(DISPLAY_ITEM_BIT);
  }

  public ItemState(@Nonnull ItemId id, ItemId prototypeId, @Nonnull String type, String view, boolean displayItem, String valueSetId, Object answer, Object value, Object defaultValue, ItemId activePage) {
    this.valueSetId = valueSetId;
    this.id = id;
    this.prototypeId = prototypeId;
    this.type = type;
    this.view = view;
    this.setBits(displayItem, DISPLAY_ITEM_BIT);
    this.answer = answer;
    this.value = value;
    this.defaultValue = defaultValue;
    this.activePage = activePage;
  }

  public ItemState(@Nonnull ItemState itemState) {
    this(itemState.getId(), itemState);
  }

  ItemState(@Nonnull ItemId id, @Nonnull ItemState itemState) {
    this.id = id;
    this.prototypeId = itemState.prototypeId;
    this.type = itemState.type;
    this.view = itemState.view;
    this.valueSetId = itemState.valueSetId;
    this.status = itemState.status;
    this.answer = itemState.answer;
    this.value = itemState.value;
    this.defaultValue = itemState.defaultValue;
    this.bits = itemState.bits;
    this.label = itemState.label;
    this.description = itemState.description;
    this.classNames = itemState.classNames;
    this.items = itemState.items;
    this.availableItems = itemState.availableItems;
    this.props = itemState.props;
    this.allowedActions = itemState.allowedActions;
    this.activePage = itemState.activePage;
  }

  @Nonnull
  public ItemId getId() {
    return id;
  }

  @Nullable
  public ItemId getPrototypeId() {
    return prototypeId;
  }

  @Nonnull
  public String getType() {
    return type;
  }

  @Nullable
  public String getView() {
    return view;
  }

  @Override
  public boolean isDisplayItem() {
    return (bits & DISPLAY_ITEM_BIT) != 0;
  }

  public Optional<String> getValueSetId() {
    return Optional.ofNullable(valueSetId);
  }

  public Status getStatus() {
    return status;
  }

  public Object getAnswer() {
    return answer;
  }

  public Object getValue() {
    return isActive() && value != null ? value : defaultValue;
  }

  @Override
  public boolean isActive() {
    return isBit(ACTIVE_BIT);
  }

  public boolean isAnswered() {
    return !isNull() && !isBlank();
  }

  public boolean isBlank() {
    return isNull() || value instanceof CharSequence && StringUtils.isBlank((CharSequence)value);
  }

  public boolean isNull() {
    return value == null;
  }

  public boolean isInvalidAnswers() {
    return isBit(INVALID_ANSWERS_BIT);
  }

  public boolean isInvalid() {
    if (answer instanceof String) {
      return value == null && StringUtils.isNotEmpty((CharSequence) answer);
    }
    return value == null && answer != null;
  }

  @Override
  public boolean isDisabled() {
    return (bits & DISABLED_BIT) != 0;
  }

  public boolean isRequired() {
    return (bits & REQUIRED_BIT) != 0;
  }

  public boolean isRowsCanBeAdded() {
    return (bits & ROWS_CAN_BE_ADDED_BIT) != 0;
  }

  public boolean isRowCanBeRemoved() {
    return (bits & ROW_CAN_BE_REMOVED_BIT) != 0;
  }

  public boolean hasCustomProps() {
    return (bits & HAS_CUSTOM_PROPS_BIT) != 0;
  }

  public String getLabel() {
    return label;
  }

  public String getDescription() {
    return description;
  }

  public List<String> getClassNames() {
    return classNames;
  }

  @Nonnull
  public List<ItemId> getItems() {
    return items;
  }

  public List<ItemId> getAvailableItems() {
    return availableItems;
  }

  public Optional<ItemId> getActivePage() {
    return Optional.ofNullable(activePage);
  }

  public Set<Action.Type> getAllowedActions() {
    return allowedActions;
  }

  @Nonnull
  public ItemState withId(@Nonnull ItemId newId) {
    return new ItemState(newId, this);
  }

  public class UpdateBuilder {

    private ItemState itemState;

    UpdateBuilder() {
    }

    private ItemState state() {
      if (itemState == null) {
        this.itemState = new ItemState(ItemState.this);
      }
      return itemState;
    }

    private boolean hasNewState() {
      return this.itemState != null;
    }

    public UpdateBuilder setStatus(Status newStatus) {
      if (status != newStatus) {
        state().status = newStatus;
      }
      return this;
    }

    public UpdateBuilder setAnswer(Object newAnswer) {
      if (!Objects.equals(answer, newAnswer)) {
        state().answer = newAnswer;
      }
      return this;
    }

    public UpdateBuilder setValue(Object newValue) {
      if (!Objects.equals(value, newValue)) {
        state().value = newValue;
      }
      return this;
    }

    public UpdateBuilder setActive(boolean newActive) {
      if (isBit(ACTIVE_BIT) != newActive) {
        state().setBits(newActive, ACTIVE_BIT);
      }
      return this;
    }

    public UpdateBuilder setDisabled(Boolean newDisabled) {
      if (newDisabled == null) {
        return this;
      }
      if (isBit(DISABLED_BIT) != newDisabled) {
        state().setBits(newDisabled, DISABLED_BIT);
      }
      return this;
    }

    public UpdateBuilder setRequired(boolean newRequired) {
      if (isBit(REQUIRED_BIT) != newRequired) {
        state().setBits(newRequired, REQUIRED_BIT);
      }
      return this;
    }

    public UpdateBuilder setRowsCanBeAdded(boolean newRowsCanBeAdded) {
      if (isBit(ROWS_CAN_BE_ADDED_BIT) != newRowsCanBeAdded) {
        state().setBits(newRowsCanBeAdded, ROWS_CAN_BE_ADDED_BIT);
      }
      return this;
    }

    public UpdateBuilder setRowCanBeRemoved(boolean newRowsCanBeRemoved) {
      if (isBit(ROW_CAN_BE_REMOVED_BIT) != newRowsCanBeRemoved) {
        state().setBits(newRowsCanBeRemoved, ROW_CAN_BE_REMOVED_BIT);
      }
      return this;
    }

    public UpdateBuilder setHasCustomProps(boolean newHasCustomProps) {
      if (isBit(HAS_CUSTOM_PROPS_BIT) != newHasCustomProps) {
        state().setBits(newHasCustomProps, HAS_CUSTOM_PROPS_BIT);
      }
      return this;
    }

    public UpdateBuilder setLabel(String newLabel) {
      if (!Objects.equals(label, newLabel)) {
        state().label = newLabel;
      }
      return this;
    }

    public UpdateBuilder setDescription(String newDescription) {
      if (!Objects.equals(description, newDescription)) {
        state().description = newDescription;
      }
      return this;
    }

    public UpdateBuilder setProp(String propName, Object newValue) {
      Object previous = props.get(propName);
      if (!Objects.equals(previous, newValue)) {
        state().props.put(propName, newValue);
      }
      return this;
    }

    public UpdateBuilder setClassNames(List<String> newClassNames) {
      if (!Objects.equals(classNames, newClassNames)) {
        state().classNames = ImmutableList.copyOf(newClassNames);
      }
      return this;
    }

    public UpdateBuilder setItems(List<ItemId> newItems) {
      if (!Objects.equals(items, newItems)) {
        state().items = ImmutableList.copyOf(newItems);
      }
      return this;
    }

    public UpdateBuilder setAvailableItems(List<ItemId> newAvailableItems) {
      if (!Objects.equals(availableItems, newAvailableItems)) {
        state().availableItems = ImmutableList.copyOf(newAvailableItems);
      }
      return this;
    }

    public UpdateBuilder setAllowedActions(Set<Action.Type> newAllowedActions) {
      if (!Objects.equals(allowedActions, newAllowedActions)) {
        state().allowedActions = ImmutableSet.copyOf(newAllowedActions);
      }
      return this;
    }

    public UpdateBuilder setActivePage(ItemId newActivePage) {
      if ((hasNewState() && state().items.contains(newActivePage) || items.contains(newActivePage)) && !Objects.equals(activePage, newActivePage)) {
        // TODO matches is active item "available"
        state().activePage = newActivePage;
      }
      return this;
    }

    public UpdateBuilder setInvalidAnswers(boolean newIsInvalidAnswers) {
      if (isBit(INVALID_ANSWERS_BIT) != newIsInvalidAnswers) {
        state().setBits(newIsInvalidAnswers, INVALID_ANSWERS_BIT);
      }
      return this;
    }

    public ItemState get() {
      if (itemState == null) {
        return ItemState.this;
      }
      return itemState;
    }
  }

  public UpdateBuilder update() {
    return new UpdateBuilder();
  }

}
