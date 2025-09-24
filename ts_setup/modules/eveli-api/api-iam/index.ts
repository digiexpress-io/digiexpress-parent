export * from './IamBackendProvider';
export * from './iam-types';
export * from './IamLiveness';


export const ROLE_AUTHORIZED = 'ROLE_Authorized';

export const mapIamRole = (role?: string|null):string => {
  // drop technical 'ROLE_' prefix from roles.
  return role?.replace("ROLE_", "") || ''
}

