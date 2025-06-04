import React from 'react';
import { CircularProgress } from '@mui/material';

import { useIam } from '@/api-iam';

import { PrefsApi } from './profile-types';
import { PreferenceContextType, PreferenceInit } from './pref-types';


import { 
  WithConfig, initWithConfig, WithVisibility, WithVisibleFields, 
  initPreference, initWithVisibility, initWithVisibleFields 
} from './initMethods';
import { useFetch } from '@dxs-ts/eveli-fetch';



export function createPrefContext(hardInit?: PreferenceInit) {

  const PreferenceProviderDelegate: React.FC<{ 
    children: React.ReactElement;
    uiSettings: PrefsApi.UiSettings | undefined;
    init: PreferenceInit;
    backend: PrefsApi.PrefsRestApi;
  }> = ({ children, uiSettings, init, backend }) => {

    const { user } = useIam();
    const userId = user.userId;
    const [state, setState] = React.useState(initPreference(init, uiSettings));

    const withConfig: WithConfig = React.useCallback((config) => initWithConfig(setState, backend, userId, config), [setState, backend, userId]);
    const withVisibility: WithVisibility = React.useCallback((visibility) => initWithVisibility(setState, backend, userId, visibility), [setState, backend, userId]);
    const withVisibleFields: WithVisibleFields = React.useCallback((visibility) => initWithVisibleFields(setState, backend, userId, visibility), [setState, backend, userId]);
    
    const contextValue: PreferenceContextType = React.useMemo(() => {
      return { pref: state, withVisibility, withVisibleFields, withConfig };
    }, [state, withVisibility, withVisibleFields, withConfig]);

    return (<PreferenceContext.Provider value={contextValue}>{children}</PreferenceContext.Provider>);
  }

  const PreferenceContext = React.createContext<PreferenceContextType>({} as any);

  const PreferenceProvider: React.FC<{ children: React.ReactElement, init?: PreferenceInit }> = (props) => {
    const { restApi } = useFetch('worker/rest/api/userprofiles/$profileId.GET', {})

    const init = hardInit ?? props.init;
    if(!init) {
      throw new Error("Preference init must be defined on props or factory method, both can't be undefined!");
    }

    const backend: PrefsApi.PrefsRestApi = React.useMemo(() => restApi(), []);
    const [state, setState] = React.useState<PrefsApi.UiSettings>();
    const [loading, setLoading] = React.useState<boolean>(true);

    React.useEffect(() => {
      backend.findUiSettings(init.id).then(userProfile => {
        setState(userProfile);
        setLoading(false);
      });
    }, []);

    if (loading) {
      return <CircularProgress />;
    }

    return (<PreferenceProviderDelegate init={init} uiSettings={state} backend={backend}>{props.children}</PreferenceProviderDelegate>);
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