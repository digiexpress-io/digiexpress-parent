import React, { PropsWithChildren } from 'react'
import { cFetch, CFetchOptions, dataLinkDelete, dataLinkFetch } from '../util/cFetch';
import { useConfig } from './ConfigContext';

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


var iapSessionRefreshWindow: Window | null = null;
var updateStarted = false;

function startSessionUpdate(loginUrl: string) {
  if (iapSessionRefreshWindow == null && !updateStarted) {
    updateStarted = true;
    let positionX = window.screenX + 30;
    let positionY = window.screenY + 30;
    iapSessionRefreshWindow = window.open(loginUrl, "_blank", `height=600,width=400,left=${positionX},top=${positionY}`);
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

function userRefreshIAPSession(loginUrl: string): Promise<void> {
  startSessionUpdate(loginUrl);
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
  const context = useConfig();
  const sessionRefreshFunction = userRefreshIAPSession.bind(null, context.loginUrl || '/oauth2/authorization/oidcprovider');
  const cFetchBound = cFetchReauth.bind(null, sessionRefreshFunction);
  const dataLinkFetchBound = dataLinkFetchReauth.bind(null, sessionRefreshFunction);
  const dataLinkDeleteBound = dataLinkDeleteReauth.bind(null, sessionRefreshFunction);

  if (!!context.loginAutoRefresh) {
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