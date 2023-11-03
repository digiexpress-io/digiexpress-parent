import React from 'react';
import Burger from '@the-wrench-io/react-burger';
import { ImmutableTabData } from './session';
import { Tab, TabEntity, TabBody, Document } from './composer-types';
import { ComposerContext, ComposerContextType, ClientContextType, ClientContext } from './Components/Context';
import ArticleTabIndicator from './Components/ArticleTabIndicator';


const isDocumentSaved = (entity: Document, ide: ComposerContextType): boolean => {
  const unsaved = Object.values(ide.session.pages).filter(p => !p.saved).filter(p => p.origin.id === entity.id);
  return unsaved.length === 0
}

const createTab = (props: { nav: TabEntity, page?: Document }) => new ImmutableTabData(props);

const handleInTabInLayout = (props: { article: Document, name?: string, id?: string }, layout: Burger.TabsContextType) => {
  console.log("Route Into Tab", props.article.id, props.id)
  const id = props.id ? props.id : props.article.id
  const nav = { value: id };

  const tab: Tab = {
    id, 
    icon: (<ArticleTabIndicator entity={ props.article } />),
    label: props.name ? props.name : props.article.id,
    data: createTab({ nav })
  };

  const oldTab = layout.session.findTab(id);
  if (oldTab !== undefined) {
    layout.actions.handleTabData(id, (oldData: TabBody) => oldData.withNav(nav));
  } else {
    // open or add the tab
    layout.actions.handleTabAdd(tab);
  }

}
const findTabInLayout = (article: Document, layout: Burger.TabsContextType): Tab | undefined => {
  const oldTab = layout.session.findTab(article.id);
  if (oldTab !== undefined) {
    const tabs = layout.session.tabs;
    const active = tabs[layout.session.history.open];
    const tab: Tab = active;
    return tab;
  }
  return undefined;
}


export const useService = () => {
  const result: ClientContextType = React.useContext(ClientContext);
  return result;
}
export const useSite = () => {
  const result: ComposerContextType = React.useContext(ComposerContext);
  return result.session.head;
}
export const useUnsaved = (entity: Document) => {
  const ide: ComposerContextType = React.useContext(ComposerContext);
  return !isDocumentSaved(entity, ide);
}
export const useComposer = () => {
  const client: ClientContextType = React.useContext(ClientContext);
  const result: ComposerContextType = React.useContext(ComposerContext);
  const isSaved = (entity: Document): boolean => isDocumentSaved(entity, result);
  return {
    session: result.session,
    actions: result.actions,
    site: result.session.head,
    isDocumentSaved: isSaved,
    client
  };
}
export const useSession = () => {
  const result: ComposerContextType = React.useContext(ComposerContext);
  return result.session;
}
export const useNav = () => {
  const layout: Burger.TabsContextType = Burger.useTabs();
  const findTab = (article: Document): Tab | undefined => {
    return findTabInLayout(article, layout);
  } 
  const handleInTab = (props: { article: Document, name?: string, id?: string }) => {
    return handleInTabInLayout(props, layout); 
  }
  return { handleInTab, findTab }
}