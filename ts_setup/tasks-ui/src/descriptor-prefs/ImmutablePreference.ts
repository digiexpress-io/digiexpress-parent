import { Preference, VisibilityRule, SortingRule, DataId, PreferenceId, ConfigRule } from './pref-types';


export interface ImmutablePreferenceInit {
  id: PreferenceId;
  backendId: string | undefined;
  fields: readonly DataId[];
  visibility: Record<DataId, VisibilityRule>;
  sorting: Record<DataId, SortingRule>;
  config: Record<DataId, ConfigRule>;
}

export class ImmutablePreference implements Preference {
  private _backendId: string | undefined;
  private _id: PreferenceId;
  private _fields: readonly DataId[];
  
  private _visibility: Record<DataId, VisibilityRule>;
  private _sorting: Record<DataId, SortingRule>;
  private _config: Record<DataId, ConfigRule>;

  constructor(init: ImmutablePreferenceInit) {
    this._id = init.id;
    this._fields = init.fields;
    this._visibility = this.initVisibility(init);
    this._sorting = init.sorting;
    this._config = init.config;
  }

  initVisibility(init: ImmutablePreferenceInit): Record<DataId, VisibilityRule> {
    const result: Record<DataId, VisibilityRule> = {};
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
  get sorting() { return Object.values(this._sorting) }

  getVisibility(dataId: DataId): VisibilityRule {
    return this._visibility[dataId];
  }
  getSorting(dataId?: DataId): SortingRule | undefined{
    return dataId ? this._sorting[dataId] : Object.values(this._sorting)[0];
  }
  getConfig(dataId: DataId): ConfigRule | undefined {
    return this._config[dataId];
  }

  withConfig(config: ConfigRule | (ConfigRule[])): ImmutablePreference {
    const nextState: Record<DataId, ConfigRule> = { ...this._config };
    const newValues: ConfigRule[] = Array.isArray(config) ? config as ConfigRule[] : [config as ConfigRule];

    for(const value of newValues) {
      nextState[value.dataId] = { ...value };
    }

    return new ImmutablePreference(this.clone({ config: nextState }));
  }

  withSorting(newValue: SortingRule): ImmutablePreference {
    const sorting: Record<DataId, SortingRule> = {  }; // composite sorting not supported yet
    sorting[newValue.dataId] = { ...newValue };
    return new ImmutablePreference(this.clone({ sorting }));
  }
  withVisibility(newValue: VisibilityRule): ImmutablePreference {
    const visibility: Record<DataId, VisibilityRule> = { ...this._visibility };
    visibility[newValue.dataId] = { ...newValue };
    return new ImmutablePreference(this.clone({ visibility }));
  }
  withVisibleFields(newValueFields: DataId[]): ImmutablePreference {
    const visibility: Record<DataId, VisibilityRule> = { };
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
      sorting: init.sorting ?? this._sorting,
      config: init.config ?? this._config,
    };
  }
}
