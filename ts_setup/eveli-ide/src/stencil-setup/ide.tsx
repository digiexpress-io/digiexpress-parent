import React from 'react';

import { StencilApi } from '@/burger'
import { ReducerDispatch, Reducer } from './Reducer';
import { SessionData } from './SessionData';

export declare namespace StencilComposerApi {

  interface SearchData {
    values: readonly SearchDataEntry[];
    filterLinks(keyword: string): readonly SearchResult[];
    filterWorkflows(keyword: string): readonly SearchResult[];
    filterArticles(keyword: string): readonly SearchResult[];
  }
  
  interface SearchDataEntry {
    id: string;
    type: "ARTICLE" | "LINK" | "WORKFLOW";
    values: readonly SearchableValue[];  
  }
  interface SearchResult {
    source: SearchDataEntry;
    matches: SearchableValue[];
  }
  
  interface SearchableValue {
    id: string;
    value: string;
    type: "ARTICLE_NAME"  | "ARTICLE_PAGE" |
          "WORKFLOW_NAME" | "WORKFLOW_LABEL" | 
          "LINK_VALUE"    | "LINK_LABEL" 
  }

  interface PageUpdate {
    saved: boolean;
    origin: StencilApi.Page;
    value: StencilApi.LocalisedContent;
    withValue(value: StencilApi.LocalisedContent): PageUpdate;
  }

  interface SessionFilter {
    locale?: StencilApi.LocaleId;
    withLocale(locale?: StencilApi.LocaleId): SessionFilter;
  }

  interface Session {
    site: StencilApi.Site,
    pages: Record<StencilApi.PageId, PageUpdate>;
    articles: ArticleView[];
    workflows: WorkflowView[];
    links: LinkView[];
    search: SearchData;
    filter: SessionFilter;

    getArticleName(articleId: StencilApi.ArticleId): { missing: boolean, name: string };
    getArticleView(articleId: StencilApi.ArticleId): ArticleView;

    getLinkView(linkId: StencilApi.LinkId): LinkView;
    getLinkName(linkId: StencilApi.LinkId): { missing: boolean, name: string };

    getWorkflowView(workflowId: StencilApi.WorkflowId): WorkflowView;
    getWorkflowName(workflowId: StencilApi.WorkflowId): { missing: boolean, name: string };

    getArticlesForLocale(locale: StencilApi.LocaleId): StencilApi.Article[];
    getArticlesForLocales(locales: StencilApi.LocaleId[]): StencilApi.Article[];

    withPage(page: StencilApi.PageId): Session;
    withPageValue(page: StencilApi.PageId, value: StencilApi.LocalisedContent): Session;
    withoutPages(pages: StencilApi.PageId[]): Session;

    withLocaleFilter(locale?: StencilApi.LocaleId): Session;
    withSite(site: StencilApi.Site): Session;
  }

  interface Actions {
    handleLoad(): Promise<void>;
    handleLoadSite(): Promise<void>;
    handlePageUpdate(page: StencilApi.PageId, value: StencilApi.LocalisedContent): void;
    handlePageUpdateRemove(pages: StencilApi.PageId[]): void;
    handleLocaleFilter(locale?: StencilApi.LocaleId): void;
  }

  interface ContextType {
    session: Session;
    actions: Actions;
    service: StencilApi.Service;
  }

  interface ArticleView {
    article: StencilApi.Article;
    pages: PageView[];
    canCreate: StencilApi.SiteLocale[];
    links: LinkView[];
    workflows: WorkflowView[];
    children: ArticleView[];
    displayOrder: number;
    getPageById(pageId: StencilApi.PageId): PageView;
    getPageByLocaleId(localeId: StencilApi.LocaleId): PageView;
    findPageByLocaleId(localeId: StencilApi.LocaleId): PageView | undefined;
  }

  interface PageView {
    title: string;
    page: StencilApi.Page;
    locale: StencilApi.SiteLocale;
  }

  interface LinkView {
    link: StencilApi.Link;
    labels: LabelView[];
  }

  interface WorkflowView {
    workflow: StencilApi.Workflow;
    labels: LabelView[];
  }

  interface LabelView {
    label: StencilApi.LocaleLabel;
    locale: StencilApi.SiteLocale;
  }
}

export namespace StencilComposerApi {
  const sessionData = new SessionData({});

  export const ComposerContext = React.createContext<ContextType>({
    session: sessionData,
    actions: {} as Actions,
    service: {} as StencilApi.Service
  });

  export const useUnsaved = (article: StencilApi.Article) => {
    const ide: ContextType = React.useContext(ComposerContext);
    return !isSaved(article, ide);
  }

  const isSaved = (article: StencilApi.Article, ide: ContextType): boolean => {
    const unsaved = Object.values(ide.session.pages).filter(p => !p.saved).filter(p => p.origin.body.article === article.id);
    return unsaved.length === 0
  }

  export const useComposer = () => {
    const result: ContextType = React.useContext(ComposerContext);
    const isArticleSaved = (article: StencilApi.Article): boolean => isSaved(article, result);

    return {
      session: result.session, service: result.service, actions: result.actions, site: result.session.site,
      isArticleSaved
    };
  }

  export const useSite = () => {
    const result: ContextType = React.useContext(ComposerContext);
    return result.session.site;
  }

  export const useSession = () => {
    const result: ContextType = React.useContext(ComposerContext);
    return result.session;
  }


  export const Provider: React.FC<{ children: React.ReactNode, service: StencilApi.Service }> = ({ children, service }) => {
    const [session, dispatch] = React.useReducer(Reducer, sessionData);
    const actions = React.useMemo(() => {
      return new ReducerDispatch(dispatch, service)
    }, [dispatch, service]);

    React.useLayoutEffect(() => {
      actions.handleLoad();
    }, [service, actions]);

    return (<ComposerContext.Provider value={{ session, actions, service }}>{children}</ComposerContext.Provider>);
  };
}

