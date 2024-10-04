import { DialobApi } from "@dxs-ts/gamut";


export function createDialobFetch(url: (string | undefined) = 'http://localhost:8080/portal/session/dialob/') {
  const fetchPost: DialobApi.FetchPOST = async (sessionId: string, actions: DialobApi.Action[], rev: number) => {
    const response = await window.fetch(`${url}${sessionId}`, {
      method: 'POST',
      body: JSON.stringify({ rev, actions }),
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  const fetchGet: DialobApi.FetchGET = async (sessionId: string) => {
    // await new Promise((res) => setTimeout(() => { }, 2000));
    const response = await window.fetch(`${url}${sessionId}`, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }
  return { fetchGet, fetchPost };
}

