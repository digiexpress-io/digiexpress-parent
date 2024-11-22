import { DialobApi } from "@dxs-ts/gamut";


export function createDialobFetch(url: (string | undefined) = '/portal/secured/actions') {
  const fetchActionPost: DialobApi.FetchActionPOST = async (sessionId: string, actions: DialobApi.Action[], rev: number) => {
    const response = await window.fetch(`${url}/fill/${sessionId}`, {
      method: 'POST',
      body: JSON.stringify({ rev, actions }),
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  const fetchActionGet: DialobApi.FetchActionGET = async (sessionId: string) => {
    // await new Promise((res) => setTimeout(() => { }, 2000));
    const response = await window.fetch(`${url}/fill/${sessionId}`, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }

  const fetchReviewGet: DialobApi.FetchReviewGET = async (sessionId: string) => {
    // await new Promise((res) => setTimeout(() => { }, 2000));
    const response = await window.fetch(`${url}/review/${sessionId}`, {
      method: 'GET',
      headers: undefined,
      credentials: undefined,
    });
    return response;
  }
  return { fetchActionGet, fetchActionPost, fetchReviewGet };
}

