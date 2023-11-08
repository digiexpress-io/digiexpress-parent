import React from 'react';
import { Backend, Profile } from 'client';
import { TenantState, TenantContextType, TenantMutator, TenantDispatch } from './descriptor-types';
import { TenantStateBuilder } from './descriptor-impl';

const Palette = {
  colors: { red: '', green: '', yellow: '', blue: '', violet: '' }
};
const TenantContext = React.createContext<TenantContextType>({} as TenantContextType);

const initState: TenantState = new TenantStateBuilder({
  activeTenant: undefined,
  activeTenantEntry: undefined,
  filtered: [],
  groupBy: 'none',
  searchString: '',
  tenantEntries: [],
  tenants: [],
  palette: {},
  groups: [],
  profile: { contentType: "OK", name: "", userId: "", today: new Date(), roles: [] }
});

const TenantProvider: React.FC<{ children: React.ReactNode, init: { backend: Backend, profile: Profile } }> = ({ children, init }) => {
  const { backend, profile } = init;
  const [loading, setLoading] = React.useState<boolean>(true);

  const [state, setState] = React.useState<TenantState>(initState.withProfile(profile));
  const setter: TenantDispatch = React.useCallback((mutator: TenantMutator) => setState(mutator), [setState]);
  const contextValue: TenantContextType = React.useMemo(() => {
    return {
      state, setState: setter, loading, palette: Palette, reload: async () => {
        return backend.tenant.getTenantEntries(state.activeTenant ?? '').then(data => {
          return setState(prev => prev.withTenantEntries(data.records))
        });
      }
    };
  }, [state, setter, loading, backend]);

  /*
    React.useEffect(() => {
      if (!loading) {
        return;
      }
      backend.tenant.getActiveTenant().then(data => {
        setLoading(false);
        setState(prev => prev.withProfile(profile).withTasks(data.records))
      });
  
    }, [loading, setLoading, backend, profile]);
  
  */

  return (<TenantContext.Provider value={contextValue}>{children}</TenantContext.Provider>);
};


export { TenantProvider, TenantContext };

