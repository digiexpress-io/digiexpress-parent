import React from 'react';

import { OrgMutatorBuilder, OrgDispatch, OrgMutator, OrgContextType } from './org-ctx-types';
import { OrgMutatorBuilderImpl, } from './org-ctx-impl';
import { Client } from './client-types';

const OrgContext = React.createContext<OrgContextType>({} as OrgContextType);

const init: OrgMutatorBuilder = new OrgMutatorBuilderImpl({
  org: { roles: [], users: {}},
  iam: {
    displayName: "" , userId: "", userRoles: [], avatar: "",
  },
});

const OrgProvider: React.FC<{ children: React.ReactNode, backend: Client }> = ({ children, backend }) => {
  
  const [loading, setLoading] = React.useState<boolean>(true);
  const [state, setState] = React.useState<OrgMutatorBuilder>(init);
  const setter: OrgDispatch = React.useCallback((mutator: OrgMutator) => setState(mutator), [setState]);

  const contextValue: OrgContextType = React.useMemo(() => {
    return { state, setState: setter, loading };
  }, [state, setter, loading]);

  React.useEffect(() => {
    if(!loading) {
      return;
    }
    backend.org().then(data => {
      setLoading(false);
      setState(prev => prev.withIam(data.user).withOrg(data.org))
    });
    
  }, [loading, setLoading, backend]);

  return (<OrgContext.Provider value={contextValue}>{children}</OrgContext.Provider>);
};


export { OrgProvider, OrgContext };

