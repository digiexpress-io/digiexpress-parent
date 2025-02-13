import React from 'react';
import { CircularProgress } from '@mui/material';

import Backend from 'descriptor-backend';
import { ImmutableAmStore, UiSettings, UserProfile, useAm } from 'descriptor-access-mgmt';

import { PreferenceContextType, PreferenceInit } from './pref-types';
import { WithSorting, WithConfig, initWithConfig, WithVisibility, WithVisibleFields, initPreference, initWithSorting, initWithVisibility, initWithVisibleFields } from './initMethods';

import LoggerFactory from 'logger';
const _logger = LoggerFactory.getLogger();


export function createPrefContext(hardInit?: PreferenceInit) {

  const PreferenceProviderDelegate: React.FC<{ 
    children: React.ReactElement;
    uiSettings: UiSettings | undefined;
    init: PreferenceInit;
  }> = ({ children, uiSettings, init }) => {

    const am = useAm();
    const userId = am.iam.id;
    const backend = Backend.useBackend();
    const [state, setState] = React.useState(initPreference(init, uiSettings));
    const withConfig: WithConfig = React.useCallback((config) => initWithConfig(setState, backend, userId, config), [setState, backend, userId]);
    const withSorting: WithSorting = React.useCallback((sorting) => initWithSorting(setState, backend, userId, sorting), [setState, backend, userId]);
    const withVisibility: WithVisibility = React.useCallback((visibility) => initWithVisibility(setState, backend, userId, visibility), [setState, backend, userId]);
    const withVisibleFields: WithVisibleFields = React.useCallback((visibility) => initWithVisibleFields(setState, backend, userId, visibility), [setState, backend, userId]);

    const contextValue: PreferenceContextType = React.useMemo(() => {
      return { pref: state, withSorting, withVisibility, withVisibleFields, withConfig };
    }, [state, withSorting, withVisibility, withVisibleFields, withConfig]);

    return (<PreferenceContext.Provider value={contextValue}>{children}</PreferenceContext.Provider>);
  }



  const PreferenceContext = React.createContext<PreferenceContextType>({} as any);

  const PreferenceProvider: React.FC<{ children: React.ReactElement, init?: PreferenceInit }> = (props) => {

    const init = hardInit ?? props.init;
    if(!init) {
      throw new Error("Preference init must be defined on props or factory method, both can't be undefined!");
    }

    const backend = Backend.useBackend();
    const [state, setState] = React.useState<UiSettings>();
    const [loading, setLoading] = React.useState<boolean>(true);

    React.useEffect(() => {
      new ImmutableAmStore(backend.store).findUiSettings(init.id).then(userProfile => {
        _logger.target({userProfile, init}).debug(`loading preference for ${init.id}`);
        setState(userProfile);
        setLoading(false);
      });
    }, []);

    if (loading) {
      return <CircularProgress />;
    }

    return (<PreferenceProviderDelegate init={init} uiSettings={state}>{props.children}</PreferenceProviderDelegate>);
  }

  function usePreference() {
    const ctx: PreferenceContextType = React.useContext(PreferenceContext);
    return ctx;
  }
  return {
    Context: PreferenceContext,
    Provider: PreferenceProvider,
    usePreference
  };
}