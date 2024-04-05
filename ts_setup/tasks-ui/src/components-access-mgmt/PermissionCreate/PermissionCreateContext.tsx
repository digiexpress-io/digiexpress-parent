import React from 'react';

export interface NewPermission {
  name: string;
  description: string;
  commitComment: string;
  roles: readonly string[]; // name or id of the role
}


export interface NewPermissionContextType {
  entity: NewPermission;
  setName(newName: string): void;
  setDescription(newDescription: string): void;
  setCommitComment(newCommitComment: string): void;
  addRole(newRole: string): void;
  removeRole(roleToRemove: string): void;
}

const NewPermissionContext = React.createContext<NewPermissionContextType>({} as any);

const NewPermissionProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [entity, setNewPermission] = React.useState<NewPermission>({
    name: '',
    description: '',
    commitComment: '',
    roles: [],
  });

  const setName = React.useCallback((name: string) => setNewPermission(previous => Object.freeze({ ...previous, name })), []);
  const setDescription = React.useCallback((description: string) => setNewPermission(previous => Object.freeze({ ...previous, description })), []);
  const setCommitComment = React.useCallback((commitComment: string) => setNewPermission(previous => Object.freeze({ ...previous, commitComment })), []);

  const addRole = React.useCallback((newRole: string) => setNewPermission(previous => {
    if (previous.roles.includes(newRole)) {
      return previous;
    }

    const updatedRoles = [...previous.roles, newRole];
    return Object.freeze({ ...previous, roles: Object.freeze(updatedRoles) })
  }), []);

  const removeRole = React.useCallback((roleToRemove: string) => setNewPermission(previous => {
    if (!previous.roles.includes(roleToRemove)) {
      return previous;
    }

    const updatedRoles = [...previous.roles.filter(role => role !== roleToRemove)];
    return Object.freeze({ ...previous, roles: Object.freeze(updatedRoles) })
  }), []);

  const contextValue: NewPermissionContextType = React.useMemo(() => {
    return {
      entity,
      setName,
      setDescription,
      setCommitComment,
      addRole,
      removeRole
    }
  }, [entity, setName, setDescription, setCommitComment, addRole, removeRole]);

  return <NewPermissionContext.Provider value={contextValue}>{children}</NewPermissionContext.Provider>
}

export function useNewPermission(): NewPermissionContextType {
  const result: NewPermissionContextType = React.useContext(NewPermissionContext);
  return result;
}

export const PermissionCreateProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return <NewPermissionProvider>{children}</NewPermissionProvider>
}



