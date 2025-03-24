import React from 'react';

import { HdesApi } from '@/api-wrench';
import { ReducerDispatch, Reducer } from './Reducer';
import { SessionData } from './SessionData';
import { useWrenchNav } from '../wrench-nav';

export declare namespace WrenchComposerApi {

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

export namespace WrenchComposerApi {
  const sessionData = new SessionData({});

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

  export const useQueryHeaders = () => {
    const branchName = useBranchName();
    const headers: Record<string, string> = {  };
    if (branchName && branchName !== "default") {
      headers["Branch-Name"] = branchName;
    }
    headers["Content-Type"] = "application/json;charset=UTF-8";
    return headers;
  }


  export const useSession = () => {
    const result: ContextType = React.useContext(ComposerContext);
    return result.session;
  }

  export const useDebug = () => {
    const { onNav } = useWrenchNav();
    const { session, actions } = useComposer();

    const handleDebugInit = (selected: HdesApi.EntityId) => {
      onNav({ type: 'DEBUG' })

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
    const service = React.useMemo<HdesApi.Service>(() => init, [init]);

    const actions = React.useMemo(() => {
      return new ReducerDispatch(dispatch, service);
    }, [dispatch, service]);

    React.useLayoutEffect(() => {
      actions.handleLoad();
    }, [service, actions]);

    return (<ComposerContext.Provider value={{ session, actions, service }}>
      {children}
    </ComposerContext.Provider>);
  };
}

