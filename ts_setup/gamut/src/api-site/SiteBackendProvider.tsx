import React from 'react';
import { SiteApi } from './site-types';
import { SiteCache } from './site-reducer';
import { useLocale } from '../api-locale';


export interface  SiteBackendProviderProps {
  fetchGet: SiteApi.FetchGET;
  children: React.ReactNode;
}


export interface SiteBackendContextType {
  site?: SiteApi.Site;
  views: Record<SiteApi.TopicId, SiteApi.TopicView>
  locale: SiteApi.LocaleCode;
  pending: boolean;
}
export const SiteBackendContext = React.createContext< SiteBackendContextType>({ pending: true, locale: 'en', views: {} });


export const SiteBackendProvider: React.FC<SiteBackendProviderProps> = (props) => {
  const { locale: selectedLocale } = useLocale();
  const fetchGet: SiteApi.FetchGET = React.useMemo(() => props.fetchGet, [props.fetchGet])
  const [site, setSite] = React.useState<SiteApi.Site>();
  const [pending, setPending] = React.useState<boolean>(true);
  const [views, setViews] = React.useState<Record<SiteApi.TopicId, SiteApi.TopicView>>({});

  // load site
  React.useEffect(() => {
    setPending(true);
    fetchGet(selectedLocale).then(async response => {
      if (!response.ok) {
        throw new SiteRequestError('Failure during fetch', response.status);
      }

      const site = await response.json();
      if (site) {
        setSite(site);
        setViews(new SiteCache(site).topics);
      }
      setPending(false);
    })
  }, [fetchGet, selectedLocale]);

  const contextValue:  SiteBackendContextType = React.useMemo(() => {
    return Object.freeze({ site, views, pending, locale: selectedLocale });
  }, [site, views, pending, selectedLocale]);

  if(pending) {
    return <></>
  }

  return (<SiteBackendContext.Provider value={contextValue}>{props.children}</ SiteBackendContext.Provider>);
}

class SiteRequestError extends Error {
  reason: string;
  code: number;
  constructor(reason: string, code: number) {
    super(reason);

    Object.setPrototypeOf(this, SiteRequestError.prototype);
    this.reason = reason;
    this.code = code;
    this.name = 'SiteRequestError';
  }
}

