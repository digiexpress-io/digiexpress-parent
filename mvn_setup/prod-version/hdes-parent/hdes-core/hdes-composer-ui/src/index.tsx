import ReactDOM from 'react-dom';
import { ThemeProvider, StyledEngineProvider } from '@mui/material/styles';
import { IntlProvider } from 'react-intl'

import 'codemirror/addon/lint/lint';
import 'codemirror/addon/hint/show-hint';
import 'codemirror/addon/scroll/simplescrollbars';
import 'codemirror/mode/groovy/groovy'; // eslint-disable-line
import 'codemirror/mode/yaml/yaml'; // eslint-disable-line

import 'codemirror/theme/eclipse.css';
import 'codemirror/lib/codemirror.css';
import 'codemirror/addon/lint/lint.css';
import 'codemirror/addon/hint/show-hint.css';
import 'codemirror/addon/scroll/simplescrollbars.css';

import { siteTheme, WrenchComposer, WrenchClient, wrenchIntl, HdesApi } from '@dxs-ts/eveli-ide';
import '@dxs-ts/eveli-ide/build/style.css';



declare global {
  interface Window {
    _env_: {
      url?: string,
      csrf?: Csrf,
      oidc?: string,
      status?: string,
    }
  }
}

interface Csrf {
  key: string, value: string
}


const getUrl = () => {
  if (window._env_ && window._env_.url) {
    const url = window._env_.url;
    if (url.endsWith("/")) {
      return url.substring(0, url.length - 1)
    }
    return url;
  }

  return "http://localhost:8081/assets";
}


const init = {
  locale: 'en',
  url: getUrl(),
  csrf: window._env_?.csrf,
  oidc: window._env_?.oidc,
  status: window._env_?.status,
};

console.log("Wrench init", init);
const store = new WrenchClient.DefaultStore(init);
const service =  new WrenchClient.ServiceImpl(store);


ReactDOM.render(
  (<IntlProvider locale={init.locale} messages={wrenchIntl[init.locale]}>
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={siteTheme}>
        <WrenchComposer service={service}/>
      </ThemeProvider>
    </StyledEngineProvider>
  </IntlProvider>),
  document.getElementById('root')
);

