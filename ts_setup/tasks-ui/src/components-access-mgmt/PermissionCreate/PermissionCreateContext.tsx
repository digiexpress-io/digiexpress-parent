import React from 'react';

export interface NewPermission {
  name: string;
  description: string;
  commitComment: string;
}


export interface NewPermissionContextType {
  entity: NewPermission;
  setName(newName: string): void;
  setDescription(newDescription: string): void;
  setCommitComment(newCommitComment: string): void;
}

const NewPermissionContext = React.createContext<NewPermissionContextType>({} as any);

const NewPermissionProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [entity, setNewPermission] = React.useState<NewPermission>({
    name: '',
    description: '',
    commitComment: '',
  });

  const setName = React.useCallback((name: string) => setNewPermission(previous => Object.freeze({ ...previous, name })), []);
  const setDescription = React.useCallback((description: string) => setNewPermission(previous => Object.freeze({ ...previous, description })), []);
  const setCommitComment = React.useCallback((commitComment: string) => setNewPermission(previous => Object.freeze({ ...previous, commitComment })), []);

  const contextValue: NewPermissionContextType = React.useMemo(() => {
    return {
      entity,
      setName,
      setDescription,
      setCommitComment,
    }
  }, [entity, setName, setDescription, setCommitComment]);

  return <NewPermissionContext.Provider value={contextValue}>{children}</NewPermissionContext.Provider>
}

export function useNewPermission(): NewPermissionContextType {
  const result: NewPermissionContextType = React.useContext(NewPermissionContext);
  return result;
}

export const PermissionCreateProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return <NewPermissionProvider>{children}</NewPermissionProvider>
}



