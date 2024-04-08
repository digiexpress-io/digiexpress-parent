import React from 'react';

import { UserProfileAndOrg } from 'descriptor-user-profile';
import { OrgState, OrgDispatch, OrgMutator, OrgContextType } from './org-ctx-types';
import { OrgMutatorBuilderImpl, } from './org-ctx-impl';
import { Backend } from 'client';

const OrgContext = React.createContext<OrgContextType>({} as OrgContextType);

const init: OrgState = new OrgMutatorBuilderImpl({
  iam: { displayName: "", userId: "", userRoles: [], avatar: '', activity: [], type: "TASK_USER" },
  org: { roles: {}, users: {} }
});

const OrgProvider: React.FC<{ children: React.ReactNode, backend: Backend, profile: UserProfileAndOrg }> = ({ children, backend, profile }) => {
  const [state, setState] = React.useState<OrgState>(init);
  const setter: OrgDispatch = React.useCallback((mutator: OrgMutator) => setState(mutator), [setState]);

  const contextValue: OrgContextType = React.useMemo(() => {
    return { state, setState: setter, profile };
  }, [state, setter, profile]);


  return (<OrgContext.Provider value={contextValue}>{children}</OrgContext.Provider>);
};


export { OrgProvider, OrgContext };

