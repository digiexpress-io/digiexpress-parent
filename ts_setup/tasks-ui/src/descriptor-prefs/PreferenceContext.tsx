import React from 'react';
import { CircularProgress } from '@mui/material';

import Backend from 'descriptor-backend';
import { ImmutableAmStore, UserProfile } from 'descriptor-access-mgmt';

import { PreferenceContextType, PreferenceInit } from './pref-types';
import { WithSorting, WithConfig, initWithConfig, WithVisibility, WithVisibleFields, initPreference, initWithSorting, initWithVisibility, initWithVisibleFields } from './initMethods';

import LoggerFactory from 'logger';
const _logger = LoggerFactory.getLogger();



export const PreferenceContext = React.createContext<PreferenceContextType>({} as any);


const PreferenceProviderDelegate: React.FC<{ 
  children: React.ReactElement;
  init: PreferenceInit;
  initProfile: UserProfile;
}> = ({ children, init, initProfile }) => {

  const { id: userId } = initProfile;
  const backend = Backend.useBackend();
  const [state, setState] = React.useState(initPreference(init, initProfile));

  const withConfig: WithConfig = React.useCallback((config) => initWithConfig(setState, backend, userId, config), [setState, backend, userId]);
  const withSorting: WithSorting = React.useCallback((sorting) => initWithSorting(setState, backend, userId, sorting), [setState, backend, userId]);
  const withVisibility: WithVisibility = React.useCallback((visibility) => initWithVisibility(setState, backend, userId, visibility), [setState, backend, userId]);
  const withVisibleFields: WithVisibleFields = React.useCallback((visibility) => initWithVisibleFields(setState, backend, userId, visibility), [setState, backend, userId]);

  const contextValue: PreferenceContextType = React.useMemo(() => {
    return { pref: state, withSorting, withVisibility, withVisibleFields, withConfig };
  }, [state, withSorting, withVisibility, withVisibleFields, withConfig]);

  return (<PreferenceContext.Provider value={contextValue}>{children}</PreferenceContext.Provider>);
}


export const PreferenceProvider: React.FC<{ children: React.ReactElement, init: PreferenceInit }> = ({ children, init }) => {

  const backend = Backend.useBackend();
  const [state, setState] = React.useState<UserProfile>();
  const [loading, setLoading] = React.useState<boolean>(true);

  React.useEffect(() => {
    new ImmutableAmStore(backend.store).getUserProfileById("current").then(userProfile => {
      _logger.target({userProfile, init}).debug(`loading preference for ${init.id}`);
      setState(userProfile);
      setLoading(false);
    });
  }, []);

  if (loading || !state) {
    return <CircularProgress />;
  }

  return (<PreferenceProviderDelegate init={init} initProfile={state}>{children}</PreferenceProviderDelegate>);
}

export function usePreference() {
  const ctx: PreferenceContextType = React.useContext(PreferenceContext);
  return ctx;
}