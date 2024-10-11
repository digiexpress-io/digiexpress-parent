import React from 'react';

import { useTheme } from '@mui/material';
import * as Burger from '@/burger';
import { BurgerApi } from '@/burger';
import { StencilApi } from '../client';
import { ReducerDispatch, Reducer } from './Reducer';
import { SessionData } from './SessionData';

declare namespace StencilComposerApi {

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

  interface Tab extends BurgerApi.TabSession<TabData> {

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

namespace StencilComposerApi {
  const sessionData = new SessionData({});
  export class ImmutableTabData implements TabData {
    private _nav: Nav;
  
    constructor(props: { nav: Nav }) {
      this._nav = props.nav;
    }
    get nav() {
      return this._nav;
    }
    withNav(nav: Nav) {
      return new ImmutableTabData({
        nav: {
          type: nav.type,
          value: nav.value === undefined ? this._nav.value : nav.value,
          value2: nav.value2 === undefined ? this._nav.value2 : nav.value2
        }
      });
    }
  }

  export const createTab = (props: { nav: Nav, page?: StencilApi.Page }) => new ImmutableTabData(props);

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

  export const useNav = () => {
    const layout = Burger.useTabs();


    const handleInTab = (props: { article: StencilApi.Article, type: NavType, locale?: string | null, secondary?: boolean }) => {
      const nav = {
        type: props.type,
        value: props.secondary ? undefined : props.locale,
        value2: props.secondary ? props.locale : undefined
      };

      let icon: React.ReactElement | undefined = undefined;
      if (props.type === "ARTICLE_PAGES") {
        icon = <ArticleTabIndicator article={props.article} type={props.type} />;
      }
      const tab: Tab = {
        id: props.article.id,
        label: props.article.body.name,
        icon,
        data: createTab({ nav })
      };

      const oldTab = layout.session.findTab(props.article.id);
      if (oldTab !== undefined) {
        layout.actions.handleTabData(props.article.id, (oldData: TabData) => oldData.withNav(nav));
      } else {
        // open or add the tab
        layout.actions.handleTabAdd(tab);
      }

    }

    const findTab = (article: StencilApi.Article): Tab | undefined => {
      const oldTab = layout.session.findTab(article.id);
      if (oldTab !== undefined) {
        const tabs = layout.session.tabs;
        const active = tabs[layout.session.history.open];
        const tab: Tab = active;
        return tab;
      }
      return undefined;
    }


    return { handleInTab, findTab };
  }

  export const Provider: React.FC<{ children: React.ReactNode, service: StencilApi.Service }> = ({ children, service }) => {
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

const ArticleTabIndicator: React.FC<{ article: StencilApi.Article, type: StencilComposerApi.NavType }> = ({ article }) => {
  const theme = useTheme();
  const { isArticleSaved } = StencilComposerApi.useComposer();
  const saved = isArticleSaved(article);
  return <span style={{
    paddingLeft: "5px",
    fontSize: '30px',
    color: theme.palette.explorerItem.contrastText,
    display: saved ? "none" : undefined
  }}>*</span>
}



export default StencilComposerApi;

