import { Preference, VisibilityRule, SortingRule, DataId, PreferenceId } from './pref-types';


export interface ImmutablePreferenceInit {
  id: PreferenceId;
  backendId: string | undefined;
  fields: readonly DataId[];
  visibility: Record<DataId, VisibilityRule>;
  sorting: Record<DataId, SortingRule>;
}

export class ImmutablePreference implements Preference {
  private _backendId: string | undefined;
  private _id: PreferenceId;
  private _fields: readonly DataId[];
  
  private _visibility: Record<DataId, VisibilityRule>;
  private _sorting: Record<DataId, SortingRule>;

  constructor(init: ImmutablePreferenceInit) {
    this._id = init.id;
    this._fields = init.fields;
    this._visibility = this.initVisibility(init);
    this._sorting = init.sorting;
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
  
  get visibility() { return Object.values(this._visibility) }
  get sorting() { return Object.values(this._sorting) }

  getVisibility(dataId: DataId): VisibilityRule {
    return this._visibility[dataId];
  }
  getSorting(dataId?: DataId): SortingRule {
    return dataId ? this._sorting[dataId] : Object.values(this._sorting)[0];
  }

  withSorting(newValue: SortingRule): ImmutablePreference {
    const sorting: Record<DataId, SortingRule> = { ...this._sorting }; // composite sorting not supported yet
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
    };
  }
}
