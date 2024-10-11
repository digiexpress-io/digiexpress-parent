import React from 'react';
import { useTheme } from '@mui/material';
import * as Burger from '@/burger';
import { BurgerApi } from '@/burger';

import { HdesApi } from '../client';
import { ReducerDispatch, Reducer } from './Reducer';
import { SessionData, ImmutableTabData } from './SessionData';

declare namespace Composer {

  interface Nav {
    value?: string | null;
  }

  interface TabData {
    nav?: Nav
    withNav(nav: Nav): TabData;
  }
  interface Tab extends BurgerApi.TabSession<TabData> {

  }

  interface DebugSession {
    error?: HdesApi.StoreError;
    debug?: HdesApi.DebugResponse;
    csv?: string;
    json?: string;

    selected: HdesApi.EntityId;
    inputType: DebugInputType;
  }
  type DebugInputType = "CSV" | "JSON";

  interface DebugSessions {
    selected?: HdesApi.EntityId,
    values: Record<HdesApi.EntityId, DebugSession>
  }

  interface PageUpdate {
    saved: boolean;
    origin: HdesApi.Entity<any>;
    value: HdesApi.AstCommand[];
    withValue(value: HdesApi.AstCommand[]): PageUpdate;
  }


  interface Session {
    site: HdesApi.Site,
    pages: Record<HdesApi.EntityId, PageUpdate>;
    debug: DebugSessions;
    branchName?: string;

    getDecision(decisionName: string): undefined | HdesApi.Entity<HdesApi.AstDecision>;
    getFlow(flowName: string): undefined | HdesApi.Entity<HdesApi.AstFlow>;
    getService(serviceName: string): undefined | HdesApi.Entity<HdesApi.AstService>;
    getEntity(id: HdesApi.EntityId): undefined | HdesApi.Entity<any>;

    withDebug(page: DebugSession): Session;
    withPage(page: HdesApi.EntityId): Session;
    withPageValue(page: HdesApi.EntityId, value: HdesApi.AstCommand[]): Session;
    withoutPages(pages: HdesApi.EntityId[]): Session;
    withBranch(branchName?: string): Session;
    withSite(site: HdesApi.Site): Session;
  }

  interface Actions {
    handleLoad(): Promise<void>;
    handleLoadSite(site?: HdesApi.Site): Promise<void>;
    handleDebugUpdate(debug: DebugSession): void;
    handlePageUpdate(page: HdesApi.EntityId, value: HdesApi.AstCommand[]): void;
    handlePageUpdateRemove(pages: HdesApi.EntityId[]): void;
    handleBranchUpdate(branchName?: string): void;
  }

  interface ContextType {
    session: Session;
    actions: Actions;
    service: HdesApi.Service;
  }
}

namespace Composer {
  const sessionData = new SessionData({});

  export const createTab = (props: { nav: Composer.Nav, page?: HdesApi.Entity<any> }) => new ImmutableTabData(props);

  export const ComposerContext = React.createContext<ContextType>({
    session: sessionData,
    actions: {} as Actions,
    service: {} as HdesApi.Service
  });

  export const useUnsaved = (entity: HdesApi.Entity<any>) => {
    const ide: ContextType = React.useContext(ComposerContext);
    return !isSaved(entity, ide);
  }

  const isSaved = (entity: HdesApi.Entity<any>, ide: ContextType): boolean => {
    const unsaved = Object.values(ide.session.pages).filter(p => !p.saved).filter(p => p.origin.id === entity.id);
    return unsaved.length === 0
  }

  export const useComposer = () => {
    const result: ContextType = React.useContext(ComposerContext);
    const isArticleSaved = (entity: HdesApi.Entity<any>): boolean => isSaved(entity, result);

    return {
      session: result.session, service: result.service, actions: result.actions, site: result.session.site,
      isArticleSaved
    };
  }

  export const useSite = () => {
    const result: ContextType = React.useContext(ComposerContext);
    return result.session.site;
  }

  export const useBranchName = () => {
    const result: ContextType = React.useContext(ComposerContext);
    return result.session.branchName;
  }

  export const useSession = () => {
    const result: ContextType = React.useContext(ComposerContext);
    return result.session;
  }
  export const useNav = () => {
    const layout = Burger.useTabs();


    const handleInTab = (props: { article: HdesApi.Entity<any> }) => {
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
    const findTab = (article: HdesApi.Entity<any>): Composer.Tab | undefined => {
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

    const handleDebugInit = (selected: HdesApi.EntityId) => {
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


  export const Provider: React.FC<{ children: React.ReactNode, service: HdesApi.Service }> = ({ children, service: init }) => {
    const [session, dispatch] = React.useReducer(Reducer, sessionData);
    const [service, setService] = React.useState<HdesApi.Service>(init);
    const branchName = session.branchName;

    React.useEffect(() => {
      setService(prev => prev.withBranch(branchName));
    }, [branchName]);

    const actions = React.useMemo(() => {
      console.log("init ide dispatch");
      return new ReducerDispatch(dispatch, service);
    }, [dispatch, service]);

    React.useLayoutEffect(() => {
      console.log("init ide data");
      actions.handleLoad();
    }, [service, actions]);

    return (<ComposerContext.Provider value={{ session, actions, service }}>
      {children}
    </ComposerContext.Provider>);
  };
}

const ArticleTabIndicator: React.FC<{ entity: HdesApi.Entity<any> }> = ({ entity }) => {
  const theme = useTheme();
  const { isArticleSaved } = Composer.useComposer();
  const saved = isArticleSaved(entity);
  return <span style={{
    paddingLeft: "5px",
    fontSize: '30px',
    color: theme.palette.explorerItem.contrastText,
    display: saved ? "none" : undefined
  }}>*</span>
}



export default Composer;

