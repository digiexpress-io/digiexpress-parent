import React from 'react';
import { useQuery } from '@tanstack/react-query'
import { SiteApi } from './site-types';
import { SiteCache } from './site-reducer';
import { useLocale } from '../api-locale';
import { getSearchTopics } from './search-topics';
import { useIntl } from 'react-intl';
import { useNavigate } from '@tanstack/react-router';
import { maintainace_en } from './fallback-content';


export interface  SiteBackendProviderProps {
  fetchSiteGet: SiteApi.FetchSiteGET;
  fetchFeedbackGet: SiteApi.FetchFeedbackGET;
  fetchFeedbackRatingPut: SiteApi.FetchFeedbackRatingPUT
  children: React.ReactNode;

  staleTime?: number | undefined;
  refetchTime?: number | false | undefined;
}


export interface SiteBackendContextType {
  site?: SiteApi.Site;
  views: Record<SiteApi.TopicId, SiteApi.TopicView>
  locale: SiteApi.LocaleCode;
  feedback: SiteApi.CustomerFeedback[];
  pending: boolean;
  voteOnReply(body: SiteApi.UpsertFeedbackRankingCommand): Promise<void>;
}
export const SiteBackendContext = React.createContext<SiteBackendContextType>({
  pending: true,
  locale: 'en',
  views: {},
  feedback: [],
  voteOnReply: (() => { }) as any
});

const staleTime = 15000;

export const SiteBackendProvider: React.FC<SiteBackendProviderProps> = (props) => {
  const nav = useNavigate();
  const { locale: selectedLocale } = useLocale();
  const intl = useIntl();
  const fetchSiteGet: SiteApi.FetchSiteGET = React.useMemo(() => props.fetchSiteGet, [props.fetchSiteGet])
  const fetchFeedbackGet: SiteApi.FetchFeedbackGET = React.useMemo(() => props.fetchFeedbackGet, [props.fetchFeedbackGet])
  const fetchFeedbackRatingPut: SiteApi.FetchFeedbackRatingPUT = React.useMemo(() => props.fetchFeedbackRatingPut, [props.fetchFeedbackRatingPut])

  // tanstack query config
  const siteQuery = useQuery({
    staleTime: props.staleTime === undefined ? staleTime : props.staleTime,
    refetchInterval: props.refetchTime === undefined ? staleTime : props.refetchTime,
    queryKey: ['sites', selectedLocale],
    queryFn: () => fetchSiteGet(selectedLocale).then(async response => {
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

  const views = siteQuery.data?.views;
  const site = siteQuery.data?.site;
  const pending = siteQuery.isPending;
  const feedback = siteQuery.isPending ? [] : (feedbackQuery.data ?? []);

  const contextValue: SiteBackendContextType = React.useMemo(() => {
    async function voteOnReply(body: SiteApi.UpsertFeedbackRankingCommand): Promise<void> {
      return fetchFeedbackRatingPut(body).then(_data => feedbackQuery.refetch()).then(_junk => { });
    }
    return Object.freeze({ site, views: views ?? {}, pending, locale: selectedLocale, feedback, voteOnReply });
  }, [site, views, pending, selectedLocale, feedback, fetchFeedbackRatingPut]);

  return (<SiteBackendContext.Provider value={contextValue}>{props.children}</SiteBackendContext.Provider>);
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

