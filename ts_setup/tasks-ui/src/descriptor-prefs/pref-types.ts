
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


export interface Preference {
  id: PreferenceId;
  fields: readonly DataId[];
  
  visibility: readonly VisibilityRule[];
  sorting: readonly SortingRule[];

  getVisibility(dataId: DataId): VisibilityRule;
  getSorting(dataId?: DataId): SortingRule;
}

export interface PreferenceContextType {
  pref: Preference;
  
  withSorting(sorting: Omit<SortingRule, "id">): void;
  withVisibility(visibility: Omit<VisibilityRule, "id">): void;
  withVisibleFields(visibility: DataId[]): void;
}


export interface PreferenceInit {
  id: PreferenceId;
  fields: DataId[];
  sorting: SortingRule; 
}