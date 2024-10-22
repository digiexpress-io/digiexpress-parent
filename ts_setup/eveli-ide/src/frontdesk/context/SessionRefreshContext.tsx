import React, { PropsWithChildren } from 'react'
import { cFetch, CFetchOptions, dataLinkDelete, dataLinkFetch } from '../util/cFetch';

/**
 * Context which opens login window if api request returns 401.
 * Typical usage of this context:
 * const session = useContext(SessionRefreshContext);
 * session.cFetch('/api/call')
      .then(response => response.json())
      .then(json=>handleResponse(json));
 * 
 */
export interface SessionRefresh {
  cFetch: (url: string, options?: CFetchOptions) => Promise<Response>
  dataLinkFetch: (url: string, options?: CFetchOptions) => Promise<Response>
  dataLinkDelete: (url: string, options?: CFetchOptions) => Promise<Response>
}


const IAP_REFRESH: boolean | undefined = process.env.VITE_IAP_REFRESH as any;
const HOST_URL = process.env.VITE_HOST_URL || 'http://localhost:3000';

var iapSessionRefreshWindow: Window | null = null;
var updateStarted = false;

function startSessionUpdate() {
  if (iapSessionRefreshWindow == null && !updateStarted) {
    updateStarted = true;
    let positionX = window.screenX + 30;
    let positionY = window.screenY + 30;
    iapSessionRefreshWindow = window.open(`${HOST_URL}/oauth2/authorization/oidcprovider`, "_blank", `height=600,width=400,left=${positionX},top=${positionY}`);
  }
  return false;
}

function checkSessionRefresh() {
  return new Promise<void>((resolve, reject) => {
    // timeout in case login is required but not logged in
    setTimeout(() => reject(), 60000);
    const loop = () => {
      cFetch(`/status`).then((response) => {
        if (response.status === 401) {
          if (iapSessionRefreshWindow != null && !iapSessionRefreshWindow.closed) {
            setTimeout(loop, 1000);
          }
          else {
            iapSessionRefreshWindow = null;
            updateStarted = false;
            reject();
          }
        } else {
          iapSessionRefreshWindow?.close();
          iapSessionRefreshWindow = null;
          updateStarted = false;
          resolve();
        }
      });
    }
    if (iapSessionRefreshWindow != null && !iapSessionRefreshWindow.closed) {
      setTimeout(loop, 1000);
    } else {
      resolve();
    }
  });
}

type refreshIAPSession = () => Promise<void>;

function userRefreshIAPSession(): Promise<void> {
  startSessionUpdate();
  return checkSessionRefresh();
}

const DefaultSessionRefresh: SessionRefresh = {
  cFetch: cFetch,
  dataLinkFetch: dataLinkFetch,
  dataLinkDelete: dataLinkDelete
}

function cFetchReauth(refreshIAPSession: refreshIAPSession, url: string, options?: CFetchOptions) {
  return cFetch(url, options)
    .then(response => {
      if (response.status === 401) {
        return refreshIAPSession()
          .then(() => {
            return cFetch(url, options);
          })
      }
      return response;
    })
}

function dataLinkFetchReauth(refreshIAPSession: refreshIAPSession, url: string, options?: CFetchOptions) {
  return dataLinkFetch(url, options)
    .then(response => {
      if (response.status === 401) {
        return refreshIAPSession()
          .then(() => {
            return dataLinkFetch(url, options);
          })
      }
      return response;
    })
}

function dataLinkDeleteReauth(refreshIAPSession: refreshIAPSession, url: string, options?: CFetchOptions) {
  return dataLinkDelete(url, options)
    .then(response => {
      if (response.status === 401) {
        return refreshIAPSession()
          .then(() => {
            return dataLinkDelete(url, options);
          })
      }
      return response;
    })
}

export const SessionRefreshContext = React.createContext<SessionRefresh>(DefaultSessionRefresh);

export const IAPSessionRefreshContext: React.FC<PropsWithChildren> = ({ children }) => {
  const cFetchBound = cFetchReauth.bind(null, userRefreshIAPSession);
  const dataLinkFetchBound = dataLinkFetchReauth.bind(null, userRefreshIAPSession);
  const dataLinkDeleteBound = dataLinkDeleteReauth.bind(null, userRefreshIAPSession);

  if (IAP_REFRESH === true) {
    return (
      <SessionRefreshContext.Provider value={{
        cFetch: cFetchBound,
        dataLinkFetch: dataLinkFetchBound,
        dataLinkDelete: dataLinkDeleteBound
      }}>
        {children}
      </SessionRefreshContext.Provider>
    )
  }
  return (<>{children}</>);
}