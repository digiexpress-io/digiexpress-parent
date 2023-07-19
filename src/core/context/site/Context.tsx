import React from 'react';
import { Service, Topic, Site } from '../../service';
import { contextReducer, SiteState, ImmutableSiteState, SiteReducerDispatch } from './contextReducer';
import { SiteContextType, initContext, SiteActionOverrides } from './ContextTypes';

interface SiteProviderProps {
  service: Service;
  defaultLocale: string;
  children: React.ReactNode; 
  overrides?: SiteActionOverrides;
}

const initState = (defaultLocale: string, service: Service): SiteState => {
  console.log("portal: site state init");
  return new ImmutableSiteState(
    defaultLocale, { 
      site: service.getSiteLoading(defaultLocale)
    });
};

const SiteProvider: React.FC<SiteProviderProps> = (props) => {
  const { overrides, defaultLocale } = props;
  const service: Service = React.useMemo(() => props.service, [props.service])
  const init = React.useMemo(() => initState(defaultLocale, service), [defaultLocale, service])
  const [state, dispatch] = React.useReducer(contextReducer, init);
  const actions = React.useMemo(() => new SiteReducerDispatch(dispatch, overrides ? overrides : {}), [dispatch, overrides]);
  const contextValue = React.useMemo(() => initContext(state, service, actions), [state, service, actions]);

  // load site
  React.useEffect(() => {
    if (!state.loaded) {
      console.log("portal: site init loader");

      service.getSite(state.locale).then(site => {
        if (site) {
          actions.setSite(site, state.locale);
        }
      })

    }
  }, [service, state.locale, state.loaded, dispatch, actions]);

  return (<SiteContext.Provider value={contextValue}>{state.loaded && props.children}</SiteContext.Provider>);
}

const SiteContext = React.createContext<SiteContextType>({
  service: {} as any,
  locale: "en",
  actions: {} as any,
  views: {},
  getBlob: (topic: Topic) => { console.log(topic); return undefined },
  setSite: (site?: Site) => console.log(site),
  setLocale: (newLocale: string) => console.log(newLocale),
})

export type { SiteProviderProps };
export { SiteContext, SiteProvider };


