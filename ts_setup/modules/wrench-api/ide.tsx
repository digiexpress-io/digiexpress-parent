import React from 'react';

import { HdesApi } from './api';
import { SessionData } from './SessionData';


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
    value: HdesApi.AstCommand[] | string;
    withValue(value: HdesApi.AstCommand[] | string): PageUpdate;
  }


  interface Session {
    site: HdesApi.Site,
    pages: Record<HdesApi.EntityId, PageUpdate>;
    debug: DebugSessions;

    getDecision(decisionName: string): undefined | HdesApi.Entity<HdesApi.AstDecision>;
    getFlow(flowName: string): undefined | HdesApi.Entity<HdesApi.AstFlow>;
    getService(serviceName: string): undefined | HdesApi.Entity<HdesApi.AstService>;
    getEntity(id: HdesApi.EntityId): undefined | HdesApi.Entity<any>;
    getLastUpdated(entityId: HdesApi.EntityId): string | null;

    withDebug(page: DebugSession): Session;
    withPage(page: HdesApi.EntityId): Session;
    withPageValue(page: HdesApi.EntityId, value: HdesApi.AstCommand[] | string): Session;
    withoutPages(pages: HdesApi.EntityId[]): Session;
    withSite(site: HdesApi.Site): Session;
    withCommitlogs(commitlogs: HdesApi.CommitLog[]): Session;
  }

  interface Actions {
    handleLoad(): Promise<void>;
    handleLoadSite(site?: HdesApi.Site): Promise<void>;
    handleDebugUpdate(debug: DebugSession): void;
    handleBranchUpdate(branchName: string | undefined): void
    handlePageUpdate(page: HdesApi.EntityId, value: HdesApi.AstCommand[] | string): void;
    handlePageUpdateRemove(pages: HdesApi.EntityId[]): void;
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
    return result.service.branchName;
  }

  export const useQueryHeaders = () => {
    const branchName = useBranchName();
    const headers: Record<string, string> = {  };
    if (branchName && branchName !== "default") {
      headers["Branch-Name"] = branchName;
    }
    headers["Content-Type"] = "application/json";
    return headers;
  }


  export const useSession = () => {
    const result: ContextType = React.useContext(ComposerContext);
    return result.session;
  }

  export const Provider: React.FC<{ 
    children: React.ReactNode, service: HdesApi.Service
  }> = ({ children, service: init }) => {
    const [session, dispatch] = React.useState(sessionData);
    const [service, setService] = React.useState<HdesApi.Service>(init);

    const actions = React.useMemo(() => {
      async function handleLoad(): Promise<void> {
        return Promise.all([service.getSite(), service.getSiteCommitLog()])
          .then(([site, commitlogs]) => {
            if(site.contentType === "NOT_CREATED") {
              service.create().site().then(created =>  dispatch((prev) => prev.withSite(created).withCommitlogs([])));
            } else {
              dispatch((prev) => prev.withSite(site).withCommitlogs(commitlogs)) 
            }
          });
      }
      async function handleLoadSite(site?: HdesApi.Site): Promise<void> {
        if(site) {
          return Promise.all([Promise.resolve(site), service.getSiteCommitLog()])
            .then(([site, commitlogs]) => dispatch((prev) => prev.withSite(site).withCommitlogs(commitlogs)));
        } else {
          return Promise.all([service.getSite(), service.getSiteCommitLog()])
            .then(([site, commitlogs]) => dispatch((prev) => prev.withSite(site).withCommitlogs(commitlogs)));
        }
      }
      function handleBranchUpdate(branchName?: string): void {
        setService(prev => prev.withBranchName(branchName))
      }
      function handleDebugUpdate(debug: WrenchComposerApi.DebugSession): void {
        dispatch((prev) => prev.withDebug(debug)) 
      }
      function handlePageUpdate(page: HdesApi.EntityId, value: HdesApi.AstCommand[] | string): void {
        dispatch((prev) => prev.withPageValue(page, value)) 
      }
      function handlePageUpdateRemove(pages: HdesApi.EntityId[]): void {
        dispatch((prev) => prev.withoutPages(pages)) 
      }

      return {
        handleLoad, handleLoadSite, handleBranchUpdate, handleDebugUpdate,
        handlePageUpdate, handlePageUpdateRemove
      }      
    }, [dispatch, service]);

    React.useLayoutEffect(() => {
      actions.handleLoad();
    }, [service, actions]);

    return (<ComposerContext.Provider value={{ session, actions, service }}>
      {children}
    </ComposerContext.Provider>);
  };
}