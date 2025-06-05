import { PrefsApi } from "./profile-types";


export interface ImmutablePreferenceInit {
  id: PrefsApi.PreferenceId;
  backendId: string | undefined;
  fields: readonly PrefsApi.DataId[];
  visibility: Record<PrefsApi.DataId, PrefsApi.VisibilityRule>;
  config: Record<PrefsApi.DataId, PrefsApi.ConfigRule>;
}

export class ImmutablePreference implements PrefsApi.Preference {
  private _backendId: string | undefined;
  private _id: PrefsApi.PreferenceId;
  private _fields: readonly PrefsApi.DataId[];
  
  private _visibility: Record<PrefsApi.DataId, PrefsApi.VisibilityRule>;
  private _config: Record<PrefsApi.DataId, PrefsApi.ConfigRule>;

  constructor(init: ImmutablePreferenceInit) {
    this._id = init.id;
    this._fields = init.fields;
    this._visibility = this.initVisibility(init);
    this._config = init.config;
  }

  initVisibility(init: ImmutablePreferenceInit): Record<PrefsApi.DataId, PrefsApi.VisibilityRule> {
    const result: Record<PrefsApi.DataId, PrefsApi.VisibilityRule> = {};
    for(const dataId of init.fields) {
      const enabled: boolean = init.visibility[dataId]?.enabled ?? true;
      result[dataId] = { dataId, enabled };
    }
    return result;
  }

  get id() { return this._id }
  get backendId() { return this._backendId }
  get fields() { return this._fields }
  get config() { return Object.values(this._config) }
  
  get visibility() { return Object.values(this._visibility) }

  getVisibility(dataId: PrefsApi.DataId): PrefsApi.VisibilityRule {
    return this._visibility[dataId];
  }
  getConfig(dataId: PrefsApi.DataId): PrefsApi.ConfigRule | undefined {
    return this._config[dataId];
  }

  withConfig(config: PrefsApi.ConfigRule | (PrefsApi.ConfigRule[])): ImmutablePreference {
    const nextState: Record<PrefsApi.DataId, PrefsApi.ConfigRule> = { ...this._config };
    const newValues: PrefsApi.ConfigRule[] = Array.isArray(config) ? config as PrefsApi.ConfigRule[] : [config as PrefsApi.ConfigRule];

    for(const value of newValues) {
      nextState[value.dataId] = { ...value };
    }

    return new ImmutablePreference(this.clone({ config: nextState }));
  }
  withVisibility(newValue: PrefsApi.VisibilityRule): ImmutablePreference {
    const visibility: Record<PrefsApi.DataId, PrefsApi.VisibilityRule> = { ...this._visibility };
    visibility[newValue.dataId] = { ...newValue };
    return new ImmutablePreference(this.clone({ visibility }));
  }
  withVisibleFields(newValueFields: PrefsApi.DataId[]): ImmutablePreference {
    const visibility: Record<PrefsApi.DataId, PrefsApi.VisibilityRule> = { };
    for(const dataId of this._fields) {
      const enabled = newValueFields.includes(dataId);
      visibility[dataId] = { dataId, enabled };
    }
    return new ImmutablePreference(this.clone({ visibility }));
  }
  clone(init: Partial<ImmutablePreferenceInit>): ImmutablePreferenceInit {
    return {
      backendId: this.backendId,
      id: this._id,
      fields: init.fields ?? this._fields,
      visibility: init.visibility ?? this._visibility,
      config: init.config ?? this._config,
    };
  }
}
