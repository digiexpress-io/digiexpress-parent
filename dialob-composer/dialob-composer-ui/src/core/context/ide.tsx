import React from 'react';
import { useTheme } from '@mui/material';

//import { StencilClient, Layout } from '../';
import DialobClient from '../client';
import Burger from '@the-wrench-io/react-burger';
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
  interface Tab extends Burger.TabSession<TabData> {

  }

  interface PageUpdate {
    saved: boolean;
    origin: DialobClient.Entity;
    value: DialobClient.AstCommand[];
    withValue(value: DialobClient.AstCommand): PageUpdate;
  }


  interface Session {
    site: DialobClient.Site,
    pages: Record<DialobClient.EntityId, PageUpdate>;

    getRelease(decisionName: string): undefined | DialobClient.Entity;
    getFormRev(decisionName: string): undefined | DialobClient.Entity;
    getForm(flowName: string): undefined | DialobClient.Form;
    getEntity(id: DialobClient.EntityId): undefined | DialobClient.Entity;

    withPage(page: DialobClient.EntityId): Session;
    withPageValue(page: DialobClient.EntityId, value: DialobClient.AstCommand[]): Session;
    withoutPages(pages: DialobClient.EntityId[]): Session;

    withSite(site: DialobClient.Site): Session;
  }

  interface Actions {
    handleLoad(): Promise<void>;
    handleLoadSite(site?: DialobClient.Site): Promise<void>;
    handlePageUpdate(page: DialobClient.EntityId, value: DialobClient.AstCommand[]): void;
    handlePageUpdateRemove(pages: DialobClient.EntityId[]): void;
  }

  interface ContextType {
    session: Session;
    actions: Actions;
    service: DialobClient.Service;
  }
}

namespace Composer {
  const sessionData = new SessionData({});

  export const createTab = (props: { nav: Composer.Nav, page?: DialobClient.Entity }) => new ImmutableTabData(props);

  export const ComposerContext = React.createContext<ContextType>({
    session: sessionData,
    actions: {} as Actions,
    service: {} as DialobClient.Service
  });

  export const useUnsaved = (entity: DialobClient.Entity) => {
    const ide: ContextType = React.useContext(ComposerContext);
    return !isSaved(entity, ide);
  }

  const isSaved = (entity: DialobClient.Entity, ide: ContextType): boolean => {
    const unsaved = Object.values(ide.session.pages).filter(p => !p.saved).filter(p => p.origin.id === entity.id);
    return unsaved.length === 0
  }

  export const useComposer = () => {
    const result: ContextType = React.useContext(ComposerContext);
    const isArticleSaved = (entity: DialobClient.Entity): boolean => isSaved(entity, result);

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


    const handleInTab = (props: { article: DialobClient.Entity, id?: string }) => {
      console.log("Route Into Tab", props.article.id, props.id)
      const id = props.id ? props.id : props.article.id
      const nav = { value: id };

      const icon = <ArticleTabIndicator entity={props.article} />;
      const tab: Composer.Tab = {
        id, icon,
        label: props.article.name ? props.article.name : props.article.id,
        data: Composer.createTab({ nav })
      };

      const oldTab = layout.session.findTab(id);
      if (oldTab !== undefined) {
        layout.actions.handleTabData(id, (oldData: Composer.TabData) => oldData.withNav(nav));
      } else {
        // open or add the tab
        layout.actions.handleTabAdd(tab);
      }

    }
    const findTab = (article: DialobClient.Entity): Composer.Tab | undefined => {
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


  export const Provider: React.FC<{ children: React.ReactNode, service: DialobClient.Service }> = ({ children, service }) => {
    const [session, dispatch] = React.useReducer(Reducer, sessionData);
    const actions = React.useMemo(() => {
      console.log("init ide dispatch");
      return new ReducerDispatch(dispatch, service)
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

const ArticleTabIndicator: React.FC<{ entity: DialobClient.Entity }> = ({ entity }) => {
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

