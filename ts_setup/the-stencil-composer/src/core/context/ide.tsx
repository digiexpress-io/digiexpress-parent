import React from 'react';

import { useTheme } from '@mui/material';
import Burger from '@the-wrench-io/react-burger';
import { StencilClient } from '../';
import { ReducerDispatch, Reducer } from './Reducer';
import { SessionData, ImmutableTabData } from './SessionData';

declare namespace Composer {

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



  type NavType = "ARTICLE_LINKS" | "ARTICLE_WORKFLOWS" | "ARTICLE_PAGES";

  interface Nav {
    type: NavType;
    value?: string | null;
    value2?: string | null;
  }

  interface TabData {
    nav?: Nav
    withNav(nav: Nav): TabData;
  }

  interface Tab extends Burger.TabSession<TabData> {

  }

  interface PageUpdate {
    saved: boolean;
    origin: StencilClient.Page;
    value: StencilClient.LocalisedContent;
    withValue(value: StencilClient.LocalisedContent): PageUpdate;
  }

  interface SessionFilter {
    locale?: StencilClient.LocaleId;
    withLocale(locale?: StencilClient.LocaleId): SessionFilter;
  }

  interface Session {
    site: StencilClient.Site,
    pages: Record<StencilClient.PageId, PageUpdate>;
    articles: ArticleView[];
    workflows: WorkflowView[];
    links: LinkView[];
    search: SearchData;
    filter: SessionFilter;

    getArticleName(articleId: StencilClient.ArticleId): { missing: boolean, name: string };
    getArticleView(articleId: StencilClient.ArticleId): ArticleView;

    getLinkView(linkId: StencilClient.LinkId): LinkView;
    getLinkName(linkId: StencilClient.LinkId): { missing: boolean, name: string };

    getWorkflowView(workflowId: StencilClient.WorkflowId): WorkflowView;
    getWorkflowName(workflowId: StencilClient.WorkflowId): { missing: boolean, name: string };

    getArticlesForLocale(locale: StencilClient.LocaleId): StencilClient.Article[];
    getArticlesForLocales(locales: StencilClient.LocaleId[]): StencilClient.Article[];

    withPage(page: StencilClient.PageId): Session;
    withPageValue(page: StencilClient.PageId, value: StencilClient.LocalisedContent): Session;
    withoutPages(pages: StencilClient.PageId[]): Session;

    withLocaleFilter(locale?: StencilClient.LocaleId): Session;
    withSite(site: StencilClient.Site): Session;
  }

  interface Actions {
    handleLoad(): Promise<void>;
    handleLoadSite(): Promise<void>;
    handlePageUpdate(page: StencilClient.PageId, value: StencilClient.LocalisedContent): void;
    handlePageUpdateRemove(pages: StencilClient.PageId[]): void;
    handleLocaleFilter(locale?: StencilClient.LocaleId): void;
  }

  interface ContextType {
    session: Session;
    actions: Actions;
    service: StencilClient.Service;
  }

  interface ArticleView {
    article: StencilClient.Article;
    pages: PageView[];
    canCreate: StencilClient.SiteLocale[];
    links: LinkView[];
    workflows: WorkflowView[];
    children: Composer.ArticleView[];
    displayOrder: number;
    getPageById(pageId: StencilClient.PageId): PageView;
    getPageByLocaleId(localeId: StencilClient.LocaleId): PageView;
    findPageByLocaleId(localeId: StencilClient.LocaleId): PageView | undefined;
  }

  interface PageView {
    title: string;
    page: StencilClient.Page;
    locale: StencilClient.SiteLocale;
  }

  interface LinkView {
    link: StencilClient.Link;
    labels: LabelView[];
  }

  interface WorkflowView {
    workflow: StencilClient.Workflow;
    labels: LabelView[];
  }

  interface LabelView {
    label: StencilClient.LocaleLabel;
    locale: StencilClient.SiteLocale;
  }
}

namespace Composer {
  const sessionData = new SessionData({});

  export const createTab = (props: { nav: Composer.Nav, page?: StencilClient.Page }) => new ImmutableTabData(props);

  export const ComposerContext = React.createContext<ContextType>({
    session: sessionData,
    actions: {} as Actions,
    service: {} as StencilClient.Service
  });

  export const useUnsaved = (article: StencilClient.Article) => {
    const ide: ContextType = React.useContext(ComposerContext);
    return !isSaved(article, ide);
  }

  const isSaved = (article: StencilClient.Article, ide: ContextType): boolean => {
    const unsaved = Object.values(ide.session.pages).filter(p => !p.saved).filter(p => p.origin.body.article === article.id);
    return unsaved.length === 0
  }

  export const useComposer = () => {
    const result: ContextType = React.useContext(ComposerContext);
    const isArticleSaved = (article: StencilClient.Article): boolean => isSaved(article, result);

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

  export const useNav = () => {
    const layout = Burger.useTabs();


    const handleInTab = (props: { article: StencilClient.Article, type: Composer.NavType, locale?: string | null, secondary?: boolean }) => {
      const nav = {
        type: props.type,
        value: props.secondary ? undefined : props.locale,
        value2: props.secondary ? props.locale : undefined
      };

      let icon: React.ReactElement | undefined = undefined;
      if (props.type === "ARTICLE_PAGES") {
        icon = <ArticleTabIndicator article={props.article} type={props.type} />;
      }
      const tab: Composer.Tab = {
        id: props.article.id,
        label: props.article.body.name,
        icon,
        data: Composer.createTab({ nav })
      };

      const oldTab = layout.session.findTab(props.article.id);
      if (oldTab !== undefined) {
        layout.actions.handleTabData(props.article.id, (oldData: Composer.TabData) => oldData.withNav(nav));
      } else {
        // open or add the tab
        layout.actions.handleTabAdd(tab);
      }

    }

    const findTab = (article: StencilClient.Article): Composer.Tab | undefined => {
      const oldTab = layout.session.findTab(article.id);
      if (oldTab !== undefined) {
        const tabs = layout.session.tabs;
        const active = tabs[layout.session.history.open];
        const tab: Composer.Tab = active;
        return tab;
      }
      return undefined;
    }


    return { handleInTab, findTab };
  }

  export const Provider: React.FC<{ children: React.ReactNode, service: StencilClient.Service }> = ({ children, service }) => {
    const [session, dispatch] = React.useReducer(Reducer, sessionData);
    const actions = React.useMemo(() => {
      console.log("init ide dispatch");
      return new ReducerDispatch(dispatch, service)
    }, [dispatch, service]);

    React.useLayoutEffect(() => {
      console.log("init ide data");
      actions.handleLoad();
    }, [service, actions]);

    return (<ComposerContext.Provider value={{ session, actions, service }}>{children}</ComposerContext.Provider>);
  };
}

const ArticleTabIndicator: React.FC<{ article: StencilClient.Article, type: Composer.NavType }> = ({ article }) => {
  const theme = useTheme();
  const { isArticleSaved } = Composer.useComposer();
  const saved = isArticleSaved(article);
  return <span style={{
    paddingLeft: "5px",
    fontSize: '30px',
    color: theme.palette.explorerItem.contrastText,
    display: saved ? "none" : undefined
  }}>*</span>
}



export default Composer;

