import { createFileFetch } from '@dxs-ts/envir-fetch';
import { IamApi } from '../api-iam';
import { useTenantConfig } from '../api-tenant-config';

export const Hook = createFileFetch('$org/userInfo.GET')({
  hook
}) 

interface InternalUserShape {
  userId?: string | undefined;
  email?: string | undefined;
  name?: string | undefined;
  roles?: string[] | undefined;
  authenticated?: boolean | undefined;
  authorized?: boolean | undefined;
  permissions?: IamApi.UserPermission[] | undefined;
}

function toFailSafeUser(json: InternalUserShape | undefined): IamApi.User {
  const roles: string[] = json?.roles ?? [];
  const permissions = json?.permissions ?? []; 

  return {
    name: json?.name ?? '',
    userId: json?.userId ?? '',
    email: json?.email ?? '',
    authenticated: json?.authenticated ?? false,
    authorized: json?.authorized ?? false,
    roles,
    permissions,

    // TODO  fix naming
    hasRole: (...roles: string[]) => (roles.filter(role => roles.indexOf(role) > -1).length > 0)
  }
}


function toWrenchOnlyUser(): IamApi.User {

  return {
    name: 'wrench-user',
    userId: 'wrench-user',
    email: 'wrench-user@resys.io',
    authenticated: true,
    authorized: true,
    roles: [],
    permissions:[
      'WRENCH_VIEW', 'WRENCH_EDIT'
    ],
    // TODO  fix naming
    hasRole: (...roles: string[]) => (roles.filter(role => roles.indexOf(role) > -1).length > 0)
  }
}

function hook(props: {}): {  
  getUser: () => Promise<IamApi.User>,
  getEmptyUser: () => IamApi.User,
} {

  const { features } = useTenantConfig();
  const params = Hook.useNativeParams();
  const { url, method } = params;
  const query = url({ org: '' }).substring(1);

  return { 

    getEmptyUser: () => toFailSafeUser({}),
    getUser: async () => {

      if(features.includes('wrench-only')) {
        return toWrenchOnlyUser();
      }
      try {
        const response = await window.fetch(query, { method });
        if(!response.ok) {
          return toFailSafeUser({});
        }
        return await response.json().then(toFailSafeUser);
      } catch(error) {
        console.error(`failed to fetch user from: ${query}`, error)
        return toFailSafeUser({});
      }
    }
   }
}