import React from 'react';
import { ThemeProvider, StyledEngineProvider } from '@mui/material/styles';
import { IntlProvider } from 'react-intl'

import { useFetch } from '@dxs-ts/eveli-fetch';
import { EveliApp } from '@/burger';

import { StencilClient, intl, siteTheme, StencilComposer, StencilComponents, StencilApi } from '@dxs-ts/eveli-ide';

const getLocale = (): 'en' | 'sv' | 'fi' => {
  let locale = (navigator.languages && navigator.languages[0]) || navigator.language || (navigator as any).userLanguage || 'en-US';
  if (locale.length > 2) {
    locale = locale.substring(0, 2);
  }
  if (['en', 'sv', 'fi'].includes(locale)) {
    return locale;
  }
  return 'en';
}


export const StencilApp: React.FC = () => {
  const locale = getLocale();
  const { getSite } = useFetch('worker/rest/api/assets/stencil.GET', {});
  const { delete: del } = useFetch('worker/rest/api/assets/stencil/$assetType.DELETE', {});
  const { create } = useFetch('worker/rest/api/assets/stencil/$assetType.POST', {});
  const { update } = useFetch('worker/rest/api/assets/stencil/$assetType.PUT', {});
  const { getReleaseContent } = useFetch('worker/rest/api/assets/stencil/releases/$releaseId.GET', {});
  const { version } = useFetch('worker/rest/api/assets/stencil/version.GET', {});

  const store: StencilApi.StencilRestApi = React.useMemo(() => ({
    getSite, delete: del, create, update, getReleaseContent, version
  }), []);

  const service = StencilClient.service({ store });
  const { Main, Secondary, Toolbar} = StencilComponents;
  return (
    <IntlProvider locale={locale} messages={intl[locale]}>
      <StyledEngineProvider injectFirst>
        <ThemeProvider theme={siteTheme}>
          <StencilComposer.Provider service={service}>
            <EveliApp main={Main} secondary={Secondary} toolbar={Toolbar} />
          </StencilComposer.Provider>
        </ThemeProvider>
      </StyledEngineProvider>
    </IntlProvider>
  );
}
