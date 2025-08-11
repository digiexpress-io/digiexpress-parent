export const checkHttpResponse = (response: Response, setLoginRequired: () => any) => {
  if (response.ok) {
    return response;
  }
  else if (response.status === 403 || response.status === 401) {
    setLoginRequired();
  }
  const error = new Error(response.statusText);
  return Promise.reject(error);
}

export const handleRejection = (ex: any, setTechnicalError: any) => {
  console.log(ex);
  setTechnicalError();
}

export const checkSearchHttpResponse = (response: Response, setLoginRequired: () => any) => {
  if (response.ok || response.status === 404) {
    return response;
  }
  else if (response.status === 403 || response.status === 401) {
    setLoginRequired();
  }
  const error = new Error(response.statusText);
  return Promise.reject(error);
}
