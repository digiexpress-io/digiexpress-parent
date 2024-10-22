export interface CFetchOptions {
  method?: RequestInit['method'];
  body?: string | any;
  headers?: RequestInit['headers'];
  credentials?: 'omit'|'include'|'same-origin';
}

function isBlob(input:any) {
  if ('Blob' in window && input instanceof Blob)
      return true;
  else return false;
}

function createBody(body: CFetchOptions['body']) {
  if(typeof body === 'string' || isBlob(body)) {
    return body;
  }
  
  if(!body) {
    return undefined;
  }

  return JSON.stringify(body);
}
// NB! This function does not handle authorization error from session timeouts, 
// thus in most cases function from SessionRefreshContext should be used which
// retries fetch after session refresh or relogin
export function cFetch(url: string, options?: CFetchOptions) {
  return fetch(url, {
    method: options?.method,
    body: createBody(options?.body),
    credentials: options?.credentials,
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  });
}

export function dataLinkFetch(url: string, options?: CFetchOptions) {
  return fetch(url, {
    method: 'PUT',
    body: createBody(options?.body),
    credentials: options?.credentials,
    headers: {
      'Content-Type': 'text/uri-list',
      ...options?.headers,
    },
  });
}

export function dataLinkDelete(url: string, options?: CFetchOptions) {
  return fetch(url, {
    method: 'DELETE',
    credentials: options?.credentials,
    headers: {
      'Content-Type': 'text/uri-list',
      ...options?.headers,
    },
  });
}

export const handleErrors = (response:Response) => {
  if (!response.ok) {
      throw Error(response.statusText);
  }
  return response;
}

