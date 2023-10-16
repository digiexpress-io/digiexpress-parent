import React from 'react';
import Burger from '@the-wrench-io/react-burger';
import { ImmutableTabData } from './session';
import { Tab, TabEntity, TabBody, Document } from './composer-types';
import { ComposerContext, ComposerContextType, ClientContextType, ClientContext } from './client-ctx';
import { UserId, RoleId } from './client-types';
import ArticleTabIndicator from './Components/ArticleTabIndicator';
import { TasksContext } from './tasks-ctx';
import { TasksContextType, TaskDescriptor } from './tasks-ctx-types';
import { OrgContext } from './org-ctx';
import { OrgContextType } from './org-ctx-types';
import { TaskEditContextType } from './task-edit-ctx-types';
import { TaskEditContext } from './task-edit-ctx'
import { ScreenContextType } from './screen-ctx-types';
import { ScreenContext } from './screen-ctx';

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
    icon: (<ArticleTabIndicator entity={props.article} />),
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

export const useScreen = () => {
  const result: ScreenContextType = React.useContext(ScreenContext);
  return result.state;
}

export const useTaskEdit = () => {
  const result: TaskEditContextType = React.useContext(TaskEditContext);
  return result;
}

export const useTasks = () => {
  const result: TasksContextType = React.useContext(TasksContext);
  return result;
}

export const useAssignees = (row: { assignees: UserId[] }) => {
  const org = useOrg();
  const [searchString, setSearchString] = React.useState<string>('');
  const searchResults = React.useMemo(() => org.state.findUsers(searchString, row.assignees), [row, searchString, org]);
  return { searchString, setSearchString, searchResults };
}

export const useRoles = (row: { roles: RoleId[] }) => {
  const org = useOrg();
  const [searchString, setSearchString] = React.useState<string>('');
  const searchResults = React.useMemo(() => org.state.findRoles(searchString, row.roles), [row, searchString, org]);
  return { searchString, setSearchString, searchResults };
}

export const useOrg = () => {
  const result: OrgContextType = React.useContext(OrgContext);
  return result;
}

export const useBackend = () => {
  const result: ClientContextType = React.useContext(ClientContext);
  return result;
}

export const useSite = () => {
  const result: ComposerContextType = React.useContext(ComposerContext);
  return result.session.profile;
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
    site: result.session.profile,
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