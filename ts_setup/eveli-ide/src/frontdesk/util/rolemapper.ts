export const FEEDBACK_ROLES = ['ROLE_Asiointi_ITAdmins','ROLE_Asiointi_ITSupporters'];
export const ROLE_AUTHORIZED = 'ROLE_Authorized';

export const mapRole = (role?: string|null):string => {
  return role?.replace("ROLE_Asiointi-", "").replace("ROLE_Asiointi_", "").replace("ROLE_", "") || ''
}

export const mapRolesList = (roles?: string[]|null):string[] => {
  if (roles) {
    return roles?.map(role=>mapRole(role))
  }
  return [];
}