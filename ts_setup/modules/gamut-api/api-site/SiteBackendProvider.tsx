import React from 'react';
import { useQuery } from '@tanstack/react-query'
import { useIntl } from 'react-intl';

import { useLocale } from '../api-locale';

import { SiteApi } from './site-types';
import { SiteCache } from './site-reducer';
import { getSearchTopics } from './search-topics';
import { maintainace_en } from './fallback-content';
import { CockpitStore } from '../api-cockpit-store';


export interface  SiteBackendProviderProps {
  fetchCockpitsGet: SiteApi.FetchCockpitsGET;
  fetchSiteGet: SiteApi.FetchSiteGET;
  fetchFeedbackGet: SiteApi.FetchFeedbackGET;
  fetchFeedbackRatingPut: SiteApi.FetchFeedbackRatingPUT;

  children: React.ReactNode;
  staleTime?: number | undefined;
  refetchTime?: number | false | undefined;
}


export interface SiteBackendContextType {
  site?: SiteApi.Site;
  views: Record<SiteApi.TopicId, SiteApi.TopicView>
  locale: SiteApi.LocaleCode;
  feedback: SiteApi.CustomerFeedback[];
  cockpits: {
    options: SiteApi.Cockpit[];
    active: SiteApi.Cockpit | null;
    setActive: (active: SiteApi.Cockpit | null | undefined) => void;
  };
  pending: boolean;
  voteOnReply(body: SiteApi.UpsertFeedbackRankingCommand): Promise<void>;
}

export const SiteBackendContext = React.createContext<SiteBackendContextType>({
  pending: true,
  locale: 'en',
  views: {},
  feedback: [],
  cockpits: {
    options: [],
    active: null,
    setActive: () => {}
  },
  voteOnReply: (() => { }) as any
});

const staleTime = 15000;

export const SiteBackendProvider: React.FC<SiteBackendProviderProps> = (props) => {
  const { locale: selectedLocale } = useLocale();
  const intl = useIntl();


  const [cockpit, setCockpit] = React.useState<SiteApi.Cockpit | null>(() => CockpitStore.get());
  const fetchCockpitsGet: SiteApi.FetchCockpitsGET = React.useMemo(() => props.fetchCockpitsGet, [props.fetchCockpitsGet])
  const fetchSiteGet: SiteApi.FetchSiteGET = React.useMemo(() => props.fetchSiteGet, [props.fetchSiteGet])
  const fetchFeedbackGet: SiteApi.FetchFeedbackGET = React.useMemo(() => props.fetchFeedbackGet, [props.fetchFeedbackGet])
  const fetchFeedbackRatingPut: SiteApi.FetchFeedbackRatingPUT = React.useMemo(() => props.fetchFeedbackRatingPut, [props.fetchFeedbackRatingPut])


  // tanstack query config
  const siteQuery = useQuery({
    staleTime: props.staleTime === undefined ? staleTime : props.staleTime,
    refetchInterval: props.refetchTime === undefined ? staleTime : props.refetchTime,
    queryKey: ['sites', selectedLocale, cockpit?.id],
    queryFn: () => fetchSiteGet(selectedLocale, cockpit?.id).then(async response => {
      if (!response.ok) {
        console.error('site not available', response.status);
        const site: SiteApi.Site = maintainace_en;
        const siteExtensions = getSearchTopics(site, intl);
        return { site, views: new SiteCache(site, siteExtensions).topics }
      }
      const site: SiteApi.Site = await response.json();
      const siteExtensions = getSearchTopics(site, intl);
      return { site, views: new SiteCache(site, siteExtensions).topics }
    }),
  });

  const feedbackQuery = useQuery({
    staleTime: props.staleTime === undefined ? staleTime : props.staleTime,
    refetchInterval: props.refetchTime === undefined ? staleTime : props.refetchTime,

    queryKey: ['feedback', selectedLocale],
    queryFn: () => fetchFeedbackGet(selectedLocale).then(async response => {
      if (!response.ok) {
        console.error('feedback not available', response.status);
        return [];
      }
      const feedback: SiteApi.CustomerFeedback[] = await response.json();
      return feedback
    }),
  });


  const cockpitsQuery = useQuery({
    staleTime: Infinity,
    refetchInterval: false,
    queryKey: ['cockpits'],
    queryFn: () => fetchCockpitsGet().then(async response => {
      if (!response.ok) {
        // just ignore on any errors, fully optional features
        const cockpits: SiteApi.Cockpit[] = [];
        return cockpits;
      }
      const cockpits: SiteApi.Cockpit[] = await response.json();
      return cockpits
    }),
  });

  const cockpits = cockpitsQuery.data ?? [];
  const views = siteQuery.data?.views;
  const site = siteQuery.data?.site ?? maintainace_en;
  const pending = siteQuery.isPending;
  const feedback = siteQuery.isPending ? [] : (feedbackQuery.data ?? []);


  const contextValue: SiteBackendContextType = React.useMemo(() => {
    async function voteOnReply(body: SiteApi.UpsertFeedbackRankingCommand): Promise<void> {
      return fetchFeedbackRatingPut(body).then(_data => feedbackQuery.refetch()).then(_junk => { });
    }

    function setActive(active: SiteApi.Cockpit | null | undefined) {
      CockpitStore.save(active);
      setCockpit(active ?? null);
    }
    
    return Object.freeze({ 
      site, 
      views: views ?? {}, 
      pending, 
      locale: selectedLocale, 
      feedback,
      cockpits: {
        options: cockpits,
        active: cockpit,
        setActive
      },
      voteOnReply
    });
  }, [site, views, pending, selectedLocale, feedback, cockpits, cockpit, fetchFeedbackRatingPut]);

  return (<SiteBackendContext.Provider value={contextValue}>{props.children}</SiteBackendContext.Provider>);
}
