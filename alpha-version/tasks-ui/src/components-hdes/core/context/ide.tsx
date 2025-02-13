import React from 'react';

import HdesClient from '../client';
import Burger from 'components-burger';
import { ReducerDispatch, Reducer } from './Reducer';
import { SessionData, ImmutableTabData } from './SessionData';
import { saffron } from 'components-colors';

import LoggerFactory from 'logger';
const log = LoggerFactory.getLogger();


declare namespace Composer {

  interface Nav {
    value?: string | null;
  }

  interface TabData {
    nav?: Nav
    withNav(nav: Nav): TabData;
  }
  interface Tab extends Burger.TabSession<TabData> {

  }

  interface DebugSession {
    error?: HdesClient.StoreError;
    debug?: HdesClient.DebugResponse;
    csv?: string;
    json?: string;

    selected: HdesClient.EntityId;
    inputType: DebugInputType;
  }
  type DebugInputType = "CSV" | "JSON";

  interface DebugSessions {
    selected?: HdesClient.EntityId,
    values: Record<HdesClient.EntityId, DebugSession>
  }

  interface PageUpdate {
    saved: boolean;
    origin: HdesClient.Entity<any>;
    value: HdesClient.AstCommand[];
    withValue(value: HdesClient.AstCommand[]): PageUpdate;
  }


  interface Session {
    site: HdesClient.Site,
    pages: Record<HdesClient.EntityId, PageUpdate>;
    debug: DebugSessions;

    getDecision(decisionName: string): undefined | HdesClient.Entity<HdesClient.AstDecision>;
    getFlow(flowName: string): undefined | HdesClient.Entity<HdesClient.AstFlow>;
    getService(serviceName: string): undefined | HdesClient.Entity<HdesClient.AstService>;
    getEntity(id: HdesClient.EntityId): undefined | HdesClient.Entity<any>;

    withDebug(page: DebugSession): Session;
    withPage(page: HdesClient.EntityId): Session;
    withPageValue(page: HdesClient.EntityId, value: HdesClient.AstCommand[]): Session;
    withoutPages(pages: HdesClient.EntityId[]): Session;

    withSite(site: HdesClient.Site): Session;
  }

  interface Actions {
    handleLoad(): Promise<void>;
    handleLoadSite(site?: HdesClient.Site): Promise<void>;
    handleDebugUpdate(debug: DebugSession): void;
    handlePageUpdate(page: HdesClient.EntityId, value: HdesClient.AstCommand[]): void;
    handlePageUpdateRemove(pages: HdesClient.EntityId[]): void;
  }

  interface ContextType {
    session: Session;
    actions: Actions;
    service: HdesClient.Service;
  }
}

namespace Composer {
  const sessionData = new SessionData({});

  export const createTab = (props: { nav: Composer.Nav, page?: HdesClient.Entity<any> }) => new ImmutableTabData(props);

  export const ComposerContext = React.createContext<ContextType>({
    session: sessionData,
    actions: {} as Actions,
    service: {} as HdesClient.Service
  });

  export const useUnsaved = (entity: HdesClient.Entity<any>) => {
    const ide: ContextType = React.useContext(ComposerContext);
    return !isSaved(entity, ide);
  }

  const isSaved = (entity: HdesClient.Entity<any>, ide: ContextType): boolean => {
    const unsaved = Object.values(ide.session.pages).filter(p => !p.saved).filter(p => p.origin.id === entity.id);
    return unsaved.length === 0
  }

  export const useComposer = () => {
    const result: ContextType = React.useContext(ComposerContext);
    const isArticleSaved = (entity: HdesClient.Entity<any>): boolean => isSaved(entity, result);

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


    const handleInTab = (props: { article: HdesClient.Entity<any> }) => {
      const nav = { value: props.article.id };

      const icon = <ArticleTabIndicator entity={props.article} />;
      const tab: Composer.Tab = {
        id: props.article.id,
        label: props.article.ast ? props.article.ast?.name : props.article.id,
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
    const findTab = (article: HdesClient.Entity<any>): Composer.Tab | undefined => {
      const oldTab = layout.session.findTab(article.id);
      if (oldTab !== undefined) {
        const tabs = layout.session.tabs;
        const active = tabs[layout.session.history.open];
        const tab: Composer.Tab = active;
        return tab;
      }
      return undefined;
    }


    return { handleInTab, findTab }
  }

  export const useDebug = () => {
    const layout = Burger.useTabs();
    const { session, actions } = useComposer();

    const handleDebugInit = (selected: HdesClient.EntityId) => {
      layout.actions.handleTabAdd({ id: 'debug', label: "Debug" })

      if (session.debug.selected && session.debug.selected !== selected) {
        const previous = session.debug.values[selected];
        if (previous) {
          actions.handleDebugUpdate(previous);
          return;
        }
      }
      actions.handleDebugUpdate({ inputType: "JSON", selected })
    }
    return { handleDebugInit }
  }


  export const Provider: React.FC<{ init: {service: HdesClient.Service}, children: React.ReactNode }> = ({ init, children }) => {
    
    const {service} = init;
    
    const [session, dispatch] = React.useReducer(Reducer, sessionData);
    const actions = React.useMemo(() => {
      log.debug("init ide dispatch");
      return new ReducerDispatch(dispatch, service)
    }, [dispatch, service]);

    React.useLayoutEffect(() => {
      log.debug("init ide data");
      actions.handleLoad();
    }, [service, actions]);

    return (<ComposerContext.Provider value={{ session, actions, service }}>
      {children}
    </ComposerContext.Provider>);
  };
}

const ArticleTabIndicator: React.FC<{ entity: HdesClient.Entity<any> }> = ({ entity }) => {
  const { isArticleSaved } = Composer.useComposer();
  const saved = isArticleSaved(entity);
  return <span style={{
    paddingLeft: "5px",
    fontSize: '30px',
    color: saffron,
    display: saved ? "none" : undefined
  }}>*</span>
}



export default Composer;

