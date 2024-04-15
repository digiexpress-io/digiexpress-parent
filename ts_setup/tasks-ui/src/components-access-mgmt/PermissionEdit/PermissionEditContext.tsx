import { ChangePermissionDescription, ChangePermissionName, Permission, PermissionId, PermissionUpdateCommand } from 'descriptor-access-mgmt';
import React from 'react';

interface EditedPermission {
  id: PermissionId;
  name: string;
  description: string;
  commitComment: string;
  roles: readonly string[]; // name or id of the role
  principals: readonly string[]; // name or id of the principal
}

export interface PermissionEditContextType {
  entity: EditedPermission;
  setName: (newName: string) => void;
  setDescription: (newDescription: string) => void;
  setCommitComment: (newComment: string) => void;
  getCommands: () => PermissionUpdateCommand[];
  //addRole: (newRole: string) => void;
  //removeRole: (roleToRemove: string) => void;
  //addPrincipal: (newPrincipal: string) => void;
  //removePrincipal: (principalToRemove: string) => void;

}

const PermissionEditContext = React.createContext<PermissionEditContextType>({} as any);

export const PermissionEditProvider: React.FC<{ children: React.ReactNode, permission: Permission }> = ({ children, permission }) => {
  const [entity, setEntity] = React.useState<EditedPermission>({
    id: permission.id,
    name: permission.name,
    description: permission.description,
    commitComment: '',
    principals: [],
    roles: []
  });

  const setName = React.useCallback((name: string) => setEntity(previous => Object.freeze({ ...previous, name })), []);
  const setDescription = React.useCallback((description: string) => setEntity(previous => Object.freeze({ ...previous, description })), []);
  const setCommitComment = React.useCallback((commitComment: string) => setEntity(previous => Object.freeze({ ...previous, commitComment })), []);

  const getCommands: () => PermissionUpdateCommand[] = React.useCallback(() => {
    const result: PermissionUpdateCommand[] = [];

    if (entity.name !== permission.name) {
      const changeName: ChangePermissionName = {
        commandType: 'CHANGE_PERMISSION_NAME', id: permission.id, name: entity.name, comment: '',
      };
      result.push(changeName);
    } else if (entity.description !== permission.description) {
      const changeDescription: ChangePermissionDescription = {
        commandType: 'CHANGE_PERMISSION_DESCRIPTION', id: permission.id, description: entity.description, comment: '',
      }
      result.push(changeDescription);
    }

    return result;
  }, [entity]);

  console.log('commit comment', entity.commitComment)

  const contextValue: PermissionEditContextType = React.useMemo(() => {
    return {
      entity,
      setName,
      setDescription,
      setCommitComment,
      getCommands
    }
  }, [entity, setName, setDescription, setCommitComment, getCommands]);

  return (<PermissionEditContext.Provider value={contextValue}>{children}</PermissionEditContext.Provider>);
}

export function usePermissionEdit(): PermissionEditContextType {
  const result: PermissionEditContextType = React.useContext(PermissionEditContext);
  return result;
}


