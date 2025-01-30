import { SiteApi } from "../api-site";
import { SearchStateImpl } from "./SearchStateImpl";
import * as ctx from "./SearchContext";


export declare namespace SearchApi {
  export type FilterMode = 'TOPICS' | 'LINKS' | 'PHONE_LINKS' | 'FORM_LINKS' | 'ALL';

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
  export const getInstance = (topics: Record<string, SiteApi.TopicView>, noValueIndicatorColon: string): SearchState => new SearchStateImpl({ source: Object.values(topics), noValueIndicatorColon });
}