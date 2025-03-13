
import React from 'react';
import { ThemeProvider, StyledEngineProvider } from '@mui/material/styles';
import { IntlProvider } from 'react-intl'


import { WrenchClient, intl, siteTheme, WrenchComposer, WrenchComponents } from '@dxs-ts/eveli-ide';
import { useFetch } from '@dxs-ts/eveli-fetch';
import { EveliApp } from '@/burger';


const init = {
  locale: 'en',
  url: "http://localhost:8081/assets", //spring-app
};

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

export const WrenchApp: React.FC = () => {
  const locale = getLocale();
  const { ast } = useFetch('worker/rest/api/assets/wrench/commands.POST', {})
  const { copy } = useFetch('worker/rest/api/assets/wrench/copyas.POST', {})
  const { getSite } = useFetch('worker/rest/api/assets/wrench/dataModels.GET', {})
  const { debug } = useFetch('worker/rest/api/assets/wrench/debugs.POST', {})
  const { diff } = useFetch('worker/rest/api/assets/wrench/diff.GET', {})
  const { importTag } = useFetch('worker/rest/api/assets/wrench/importTag.POST', {})
  const { createAsset } = useFetch('worker/rest/api/assets/wrench/resources.POST', {})
  const { update } = useFetch('worker/rest/api/assets/wrench/resources.PUT', {})
  const { remove } = useFetch('worker/rest/api/assets/wrench/resources/$id.DELETE', {})
  const { summary } = useFetch('worker/rest/api/assets/wrench/summary/$tagId.GET', {})
  const { version } = useFetch('worker/rest/api/assets/wrench/version.GET', {})


  const service = React.useMemo(() => new WrenchClient.ServiceImpl({
    update, createAsset, ast, getSite, debug, copy, version, diff, summary, remove, importTag,
  }), [update, createAsset, ast, getSite, debug, copy, version, diff, summary, remove, importTag]);
  const { Main, Secondary, Toolbar } = WrenchComponents;

  return (
    <IntlProvider locale={locale} messages={intl[locale]}>
      <StyledEngineProvider injectFirst>
        <ThemeProvider theme={siteTheme}>
          <WrenchComposer.Provider service={service}>
            <EveliApp main={Main} secondary={Secondary} toolbar={Toolbar} />
          </WrenchComposer.Provider>
        </ThemeProvider>
      </StyledEngineProvider>
    </IntlProvider>
  );
}
