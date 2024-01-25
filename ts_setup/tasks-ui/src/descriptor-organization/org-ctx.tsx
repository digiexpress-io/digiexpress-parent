import React from 'react';

import { UserProfileAndOrg } from 'client';
import { OrgState, OrgDispatch, OrgMutator, OrgContextType } from './org-ctx-types';
import { OrgMutatorBuilderImpl, } from './org-ctx-impl';
import { Backend } from 'client';

const OrgContext = React.createContext<OrgContextType>({} as OrgContextType);

const init: OrgState = new OrgMutatorBuilderImpl({
  iam: { displayName: "", userId: "", userRoles: [], avatar: '', activity: [], type: "TASK_USER" },
  org: { roles: {}, users: {} }
});

const OrgProvider: React.FC<{ children: React.ReactNode, backend: Backend, profile: UserProfileAndOrg }> = ({ children, backend, profile }) => {

  const [loading, setLoading] = React.useState<boolean>(true);
  const [state, setState] = React.useState<OrgState>(init);
  const setter: OrgDispatch = React.useCallback((mutator: OrgMutator) => setState(mutator), [setState]);

  const contextValue: OrgContextType = React.useMemo(() => {
    return { state, setState: setter, loading, profile };
  }, [state, setter, loading, profile]);

  React.useEffect(() => {
    if (!loading) {
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

