import React from 'react';
import { CircularProgress } from '@mui/material';

import Context from 'context';
import { UserProfile } from 'client';

import { PreferenceContextType, Preference, PreferenceId, PreferenceInit, VisibilityRule, SortingRule } from './pref-types';
import { WithSorting, WithVisibility, initPreference, initWithSorting, initWithVisibility } from './initMethods';

import LoggerFactory from 'logger';
const _logger = LoggerFactory.getLogger();



export const PreferenceContext = React.createContext<PreferenceContextType>({} as any);


const PreferenceProviderDelegate: React.FC<{ 
  children: React.ReactElement;
  init: PreferenceInit;
  initProfile: UserProfile;
}> = ({ children, init, initProfile }) => {

  const { id: userId } = initProfile;
  const backend = Context.useBackend();
  const [state, setState] = React.useState(initPreference(init, initProfile));

  const withSorting: WithSorting = React.useCallback((sorting) => initWithSorting(setState, backend, userId, sorting), [setState, backend, userId]);
  const withVisibility: WithVisibility = React.useCallback((visibility) => initWithVisibility(setState, backend, userId, visibility), [setState, backend, userId]);

  const contextValue: PreferenceContextType = React.useMemo(() => {
    return { pref: state, withSorting, withVisibility };
  }, [state, withSorting, withVisibility]);

  return (<PreferenceContext.Provider value={contextValue}>{children}</PreferenceContext.Provider>);
}


export const PreferenceProvider: React.FC<{ children: React.ReactElement, init: PreferenceInit }> = ({ children, init }) => {

  const backend = Context.useBackend();
  const [state, setState] = React.useState<UserProfile>();
  const [loading, setLoading] = React.useState<boolean>(true);

  React.useEffect(() => {
    backend.currentUserProfile().then(userProfile => {
      _logger.target({userProfile, init}).debug(`loading preference for ${init.id}`);
      setState(userProfile.user);
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