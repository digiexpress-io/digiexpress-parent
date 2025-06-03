

import { PreferenceInit, VisibilityRule, SortingRule, DataId, ConfigRule } from './pref-types';
import { ImmutablePreference } from './ImmutablePreference';
import { PrefsApi } from './profile-types';



export type WithVisibleFields = (visibleFields: DataId[]) => void;
export type WithSorting = (sorting: Omit<SortingRule, "id">) => void;
export type WithVisibility = (visibility: Omit<VisibilityRule, "id">) => void;
export type WithConfig = (config: ConfigRule | (ConfigRule[])) => void;


async function storeSettings(backend: PrefsApi.PrefsRestApi, userId: string, pref: ImmutablePreference): Promise<void> {
  const command: PrefsApi.UpsertUiSettings = {
    userId,
    commandType: 'UpsertUiSettings',

    settingsId: pref.id,
    sorting: pref.sorting,
    visibility: pref.visibility,
    config: pref.config
    
  };
  await backend.updateUiSettings(command);
}

export function initPreference(
  init: PreferenceInit, initProfile: PrefsApi.UiSettings | undefined
): ImmutablePreference {
  const { id } = init;
  const fields = Object.freeze(init.fields);
  const sorting: Record<string, SortingRule> = {};
  const visibility: Record<string, VisibilityRule> = {};
  const config: Record<string, ConfigRule> = {};
  const stored = initProfile;

  // defaults first
  if(init.sorting) {
    sorting[init.sorting.dataId] = init.sorting;
    init.fields.forEach(field => visibility[field] = { dataId: field, enabled: true });
  }
  // backend
  if(stored) {
    stored.sorting.forEach(e => sorting[e.dataId] = e);
    stored.visibility.forEach(e => visibility[e.dataId] = e);
    stored.config?.forEach(e => config[e.dataId] = e);
  }
  return new ImmutablePreference({ id, fields, sorting, visibility, backendId: stored?.id, config });
}

export function parsePreference(
  settingsId: string, initProfile:PrefsApi.UiSettings | undefined
): ImmutablePreference {
  const fields: string[] = [];
  const sorting: Record<string, SortingRule> = {};
  const visibility: Record<string, VisibilityRule> = {};
  const config: Record<string, ConfigRule> = {};
  const stored = initProfile;

  // backend
  if(stored) {
    stored.sorting.forEach(e => sorting[e.dataId] = e);
    stored.visibility.forEach(e => visibility[e.dataId] = e);
    stored.config?.forEach(e => config[e.dataId] = e);
  }
  return new ImmutablePreference({ id: settingsId, fields, sorting, visibility, backendId: stored?.id, config });
}

export function initWithConfig(
  setPref: React.Dispatch<React.SetStateAction<ImmutablePreference>>,
  backend: PrefsApi.PrefsRestApi, 
  userId: string, 
  config: ConfigRule | (ConfigRule[])
) {

  setPref(currentState => {
    if(!Array.isArray(config)) {
      const noChanges = currentState.getConfig(config.dataId)?.value === config.value;
      if(noChanges) {
        return currentState;
      }
    } else {

    }

    const nextState = currentState.withConfig(config);
    storeSettings(backend, userId, nextState);
    return nextState;
  });
}

export function initWithSorting(
  setPref: React.Dispatch<React.SetStateAction<ImmutablePreference>>,
  backend: PrefsApi.PrefsRestApi, 
  userId: string, 
  sorting: SortingRule
) {

  setPref(currentState => {
    const nextState = currentState.withSorting(sorting);
    storeSettings(backend, userId, nextState);
    return nextState;
  });
}

export function initWithVisibleFields(
  setPref: React.Dispatch<React.SetStateAction<ImmutablePreference>>,
  backend: PrefsApi.PrefsRestApi, 
  userId: string, 
  visibility: DataId[]) {


  setPref(currentState => {
    const nextState = currentState.withVisibleFields(visibility);
    storeSettings(backend, userId, nextState);
    return nextState;
  });
}


export function initWithVisibility(
  setPref: React.Dispatch<React.SetStateAction<ImmutablePreference>>,
  backend: PrefsApi.PrefsRestApi, 
  userId: string, 
  visibility: VisibilityRule) {

  setPref(currentState => {
    const nextState = currentState.withVisibility(visibility);
    storeSettings(backend, userId, nextState);
    return nextState;
  });
}