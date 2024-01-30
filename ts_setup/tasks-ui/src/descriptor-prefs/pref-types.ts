
export type PreferenceId = string;
export type DataId = string;

export interface VisibilityRule {
  dataId: DataId;
  enabled: boolean;
}

export interface SortingRule {
  dataId: DataId;
  direction: 'asc' | 'desc';
}

export interface ConfigRule {
  dataId: DataId;
  value: string;
}

export interface Preference {
  id: PreferenceId;
  fields: readonly DataId[];
  
  visibility: readonly VisibilityRule[];
  sorting: readonly SortingRule[];
  config: readonly ConfigRule[];

  getVisibility(dataId: DataId): VisibilityRule;
  getConfig(dataId: DataId): ConfigRule | undefined;
  getSorting(dataId?: DataId): SortingRule | undefined;
}

export interface PreferenceContextType {
  pref: Preference;
  
  withConfig(config: ConfigRule): void;
  withSorting(sorting: Omit<SortingRule, "id">): void;
  withVisibility(visibility: Omit<VisibilityRule, "id">): void;
  withVisibleFields(visibility: DataId[]): void;
}


export interface PreferenceInit {
  id: PreferenceId;
  fields: DataId[];
  sorting: SortingRule; 
}