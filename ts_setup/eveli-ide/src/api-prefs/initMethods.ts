


import { ImmutablePreference } from './ImmutablePreference';
import { PrefsApi } from './profile-types';



export type WithVisibleFields = (visibleFields: PrefsApi.DataId[]) => void;
export type WithVisibility = (visibility: Omit<PrefsApi.VisibilityRule, "id">) => void;
export type WithConfig = (config: PrefsApi.ConfigRule | (PrefsApi.ConfigRule[])) => void;


async function storeSettings(backend: PrefsApi.PrefsRestApi, userId: string, pref: ImmutablePreference): Promise<void> {
  const command: PrefsApi.UpsertUiSettings = {
    userId,
    commandType: 'UpsertUiSettings',

    settingsId: pref.id,
    visibility: pref.visibility,
    config: pref.config
    
  };
  await backend.updateUiSettings(command);
}

export function parsePreference(
  settingsId: string, initProfile:PrefsApi.UiSettings | undefined
): ImmutablePreference {
  const fields: string[] = [];
  const visibility: Record<string, PrefsApi.VisibilityRule> = {};
  const config: Record<string, PrefsApi.ConfigRule> = {};
  const stored = initProfile;

  // backend
  if(stored) {
    stored.visibility.forEach(e => visibility[e.dataId] = e);
    stored.config?.forEach(e => config[e.dataId] = e);
  }
  return new ImmutablePreference({ id: settingsId, fields, visibility, backendId: stored?.id, config });
}

export function initWithConfig(
  setPref: React.Dispatch<React.SetStateAction<ImmutablePreference>>,
  backend: PrefsApi.PrefsRestApi, 
  userId: string, 
  config: PrefsApi.ConfigRule | (PrefsApi.ConfigRule[])
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

export function initWithVisibleFields(
  setPref: React.Dispatch<React.SetStateAction<ImmutablePreference>>,
  backend: PrefsApi.PrefsRestApi, 
  userId: string, 
  visibility: PrefsApi.DataId[]) {


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
  visibility: PrefsApi.VisibilityRule) {

  setPref(currentState => {
    const nextState = currentState.withVisibility(visibility);
    storeSettings(backend, userId, nextState);
    return nextState;
  });
}