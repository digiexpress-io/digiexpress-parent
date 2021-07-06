import ReactDOM from 'react-dom';
import React from 'react';
import { ThemeProvider } from '@material-ui/core';
import { IntlProvider } from 'react-intl';
import { StyledEngineProvider } from '@material-ui/core/styles';
import reportWebVitals from './reportWebVitals';
import { ReportHandler } from 'web-vitals';

import { theme } from './themes';
import messages from './intl';

import { Resource, Hdes } from '../';


const reportWebVitals = (onPerfEntry?: ReportHandler) => {
  if (onPerfEntry && onPerfEntry instanceof Function) {
    import('web-vitals').then(({ getCLS, getFID, getFCP, getLCP, getTTFB }) => {
      getCLS(onPerfEntry);
      getFID(onPerfEntry);
      getFCP(onPerfEntry);
      getLCP(onPerfEntry);
      getTTFB(onPerfEntry);
    });
  }
};


declare global {
  interface Window {
    _env_: {
      url?: string,
      csrf?: Csrf,
    }
  }
}

interface Csrf { 
  key: string, value: string
}


const getUrl = () => {
  if(window._env_.url) {
    const url = window._env_.url;
    if(url.endsWith("/")) {
      return url.substring(0, url.length - 2)
    }
    return url;
  }
  
  return "http://localhost:8081/assets";
}

console.log("WINDOW CONFIG", window._env_);

const init = {
  locale: 'en',
  url: getUrl(),
  csrf: window._env_.csrf
};

console.log("INIT", init);


const store: Hdes.Store = {
  fetch<T>(path: string, req?: RequestInit): Promise<T> {
    if (!path) {
      throw new Error("can't fetch with undefined url")
    }

    const defRef: RequestInit = {
      method: "GET",
      credentials: 'same-origin',
      headers: {
        "Content-Type": "application/json;charset=UTF-8"
      }
    };
    
    if(init.csrf) {
      const headers: Record<string, string> = defRef.headers as any;
      headers[init.csrf.key] = init.csrf.value;
    }

    const url = init.url;
    const finalInit: RequestInit = Object.assign(defRef, req ? req : {});


    return fetch(url + path, finalInit)
      .then(response => {
        if (response.status === 302) {
          return null;
        }
        if (!response.ok) {
          return response.json().then(data => {
            console.error(data);
            throw new Hdes.StoreError({
              text: response.statusText,
              status: response.status,
              errors: data
            });
          });
        }
        return response.json();
      })
  }
};

const NewApp = (
  <StyledEngineProvider injectFirst>
    <ThemeProvider theme={theme}>
      <IntlProvider locale={init.locale} messages={messages[init.locale]}>
        <Resource.Editor store={store} theme='dark' />
      </IntlProvider>
    </ThemeProvider>
  </StyledEngineProvider>);


ReactDOM.render(
  NewApp,
  document.getElementById('root')
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
