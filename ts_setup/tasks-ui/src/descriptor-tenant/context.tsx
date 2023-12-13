import React from 'react';
import { Backend, UserProfileAndOrg } from 'client';
import { TenantState, TenantContextType, TenantMutator, TenantDispatch } from './types';
import { TenantStateBuilder } from './context-state';

const Palette = {
  colors: { red: '', green: '', yellow: '', blue: '', violet: '' }
};
const TenantContext = React.createContext<TenantContextType>({} as TenantContextType);

const initState: TenantState = new TenantStateBuilder({
  activeTenant: undefined,
  activeTenantEntry: undefined,
  tenantEntries: [],
  tenants: [],
  palette: {},
  profile: {
    user: {
      created: new Date(),
      updated: new Date(),
      details: {
        email: '',
        username: '',
        firstName: '',
        lastName: ''
      },
      notificationSettings: [{
        type: '',
        enabled: true
      }]
    }, userId: "", today: new Date(), roles: []
  }
});

const TenantProvider: React.FC<{ children: React.ReactNode, init: { backend: Backend, profile: UserProfileAndOrg } }> = ({ children, init }) => {
  const { backend, profile } = init;
  const [loading, setLoading] = React.useState<boolean>(true);

  const [state, setState] = React.useState<TenantState>(initState.withProfile(profile));
  const setter: TenantDispatch = React.useCallback((mutator: TenantMutator) => setState(mutator), [setState]);
  const contextValue: TenantContextType = React.useMemo(() => {
    return {
      state, setState: setter, loading, palette: Palette, reload: async () => {
        return backend.tenant.getTenantEntries(state.activeTenant ?? '').then(data => {
          console.log('reload active tenant', state.activeTenant);
          return setState(prev => prev.withTenantEntries(data.records))
        });
      }
    };
  }, [state, setter, loading, backend]);


  React.useEffect(() => {
    if (!loading) {
      return;
    }
    backend.tenant.getTenants().then(data => {
      setLoading(false);
      console.log(data);

      if (data.length === 1) {
        const [{ id }] = data;
        backend.tenant.getTenantEntries(id).then(entries => {
          setState(prev => prev.withActiveTenant(id).withProfile(profile).withTenants(data).withTenantEntries(entries.records));
        });
      } else {
        setState(prev => prev.withProfile(profile).withTenants(data))
      }
    });

  }, [loading, setLoading, backend, profile]);



  return (<TenantContext.Provider value={contextValue}>{children}</TenantContext.Provider>);
};


export { TenantProvider, TenantContext };

