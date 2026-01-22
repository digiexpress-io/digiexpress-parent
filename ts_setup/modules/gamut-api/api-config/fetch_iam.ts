import { IamApi } from "../api-iam";

export function createIamFetch(url: (string | undefined) = '/portal/secured/iam') {
  const fetchUserGET: IamApi.FetchUserGET = async () => {
    const response = await window.fetch(`${url}`, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  const fetchUserRolesGET: IamApi.FetchUserRolesGET = async () => {
    const response = await window.fetch(`${url}/roles`, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  const fetchUserProductsGET: IamApi.FetchUserProductsGET = async (cockpitId?: string) => {
    const tenant = cockpitId ? `?cockpitId=${cockpitId}` : '';
    const response = await window.fetch(`/portal/secured/actions/authorizations${tenant}`, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  const fetchUserLivenessGET: IamApi.FetchUserLivenessGET = async () => {
    const response = await window.fetch(`${url}/liveness`, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  return { fetchUserGET, fetchUserRolesGET, fetchUserProductsGET, fetchUserLivenessGET };
}

