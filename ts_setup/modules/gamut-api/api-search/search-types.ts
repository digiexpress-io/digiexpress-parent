import { SiteApi } from "../api-site";
import { SearchStateImpl } from "./SearchStateImpl";
import * as ctx from "./SearchContext";
import * as backend from "./useBackendSearch";
import * as semantic from "./backend-results";


export declare namespace SearchApi {
  export type FilterMode = 'TOPICS' | 'LINKS' | 'PHONE_LINKS' | 'FORM_LINKS' | 'ALL';

  export interface BackendSearchResult {
    workflowId: SiteApi.TopicLinkId;
    topicId?: SiteApi.TopicId;
    title: string;
    category?: string;
    locale: SiteApi.LocaleCode;
    score: number;
  }

  export interface BackendSearchResponse {
    query: string;
    locale: SiteApi.LocaleCode;
    fallback?: boolean;
    results: BackendSearchResult[];
  }

  export type BackendSearchStatus = 'unavailable' | 'pending' | 'ready';

  export interface BackendSearchState {
    status: BackendSearchStatus;
    results: BackendSearchResult[] | undefined;
    loading: boolean;
    showLoader: boolean;
    query: string | undefined;
  }

  export interface SemanticResults {
    topics: SiteApi.TopicView[];
    forms: LinkToForm[];
  }

  export interface LinkToForm {
    linkToForm: SiteApi.TopicLink,
    topic: SiteApi.TopicView,
    label: string
  }


  export interface SearchState {
    searchString: string | undefined;
    searchOptionType: FilterMode;
    topics: readonly SiteApi.TopicView[];
    external: readonly SiteApi.TopicLink[];
    internal: readonly SiteApi.TopicLink[];
    phones: readonly SiteApi.TopicLink[];
    forms: readonly LinkToForm[];
    find(newSearchString: string): SearchState;
    filterMode(type: FilterMode): SearchState;
  }

  export interface SearchContextType {
    value: SearchState;
    find(newSearchString: string): void;
    filterMode(type: FilterMode): void;
  }
}

export namespace SearchApi {
  export const SearchProvider = ctx.SearchProvider;
  export const useSearch = ctx.useSearch;
  export const useBackendSearch = backend.useBackendSearch;
  export const useSemanticResults = semantic.useSemanticResults;
  export const getInstance = (topics: Record<string, SiteApi.TopicView>, noValueIndicatorColon: string): SearchState => new SearchStateImpl({ source: Object.values(topics), noValueIndicatorColon });
}

