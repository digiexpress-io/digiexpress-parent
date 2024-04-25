package io.resys.userprofile.client.api.model;

import java.io.Serializable;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable @JsonSerialize(as = ImmutableUiSettings.class) @JsonDeserialize(as = ImmutableUiSettings.class)
public
interface UiSettings extends Serializable {
  String getSettingsId();
  List<UiSettings.UiSettingForConfig> getConfig();
  List<UiSettings.UiSettingForVisibility> getVisibility();
  List<UiSettings.UiSettingsForSorting> getSorting();
  

  @Value.Immutable @JsonSerialize(as = ImmutableUiSettingForVisibility.class) @JsonDeserialize(as = ImmutableUiSettingForVisibility.class)
  interface UiSettingForVisibility extends Serializable {
    String getDataId();
    Boolean getEnabled();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableUiSettingForConfig.class) @JsonDeserialize(as = ImmutableUiSettingForConfig.class)
  interface UiSettingForConfig extends Serializable {
    String getDataId();
    String getValue();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableUiSettingsForSorting.class) @JsonDeserialize(as = ImmutableUiSettingsForSorting.class)
  interface UiSettingsForSorting extends Serializable {
    String getDataId();
    String getDirection();
  }
}