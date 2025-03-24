export * from './IamBackendProvider';
export * from './iam-types';
export * from './IamLiveness';



export const FEEDBACK_ROLES = ['ROLE_Asiointi_ITAdmins','ROLE_Asiointi_ITSupporters'];
export const ROLE_AUTHORIZED = 'ROLE_Authorized';

export const mapIamRole = (role?: string|null):string => {
  return role?.replace("ROLE_Asiointi-", "").replace("ROLE_Asiointi_", "").replace("ROLE_", "") || ''
}

export const mapIamRolesList = (roles?: string[]|null):string[] => {
  if (roles) {
    return roles?.map(role => mapIamRole(role))
  }
  return [];
}