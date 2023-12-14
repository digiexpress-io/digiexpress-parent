import React from 'react';
import Burger from 'components-burger';
import { useTheme } from '@mui/material';
import { Tab, TabEntity, TabBody, Document } from './composer-ctx-types';
import { ImmutableTabData } from './composer-ctx-impl';
import { ComposerContext, ComposerContextType, ClientContextType, ClientContext } from './client-ctx';
import { UserId, RoleId, RepoType } from 'client';

import { ProjectsContextType, ProjectsContext } from 'descriptor-project';
import { OrgContext, OrgContextType } from 'descriptor-organization';
import { TenantContext, TenantContextType } from 'descriptor-tenant';
import { TasksContext, TasksContextType, TaskEditContext, TaskEditContextType } from 'descriptor-task';
import { TenantConfigContext, TenantConfigContextType } from 'descriptor-tenant-config';

const ArticleTabIndicator: React.FC<{ entity: Document }> = ({ entity }) => {
  const theme = useTheme();
  const { isDocumentSaved } = useComposer();
  const saved = isDocumentSaved(entity);
  return <span style={{
    paddingLeft: "5px",
    fontSize: '30px',
    color: theme.palette.explorerItem.contrastText,
    display: saved ? "none" : undefined
  }}>*</span>
}



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

export const useTaskEdit = () => {
  const result: TaskEditContextType = React.useContext(TaskEditContext);
  return result;
}

export const useTasks = () => {
  const result: TasksContextType = React.useContext(TasksContext);
  return result;
}
export const useProjects = () => {
  const result: ProjectsContextType = React.useContext(ProjectsContext);
  return result;
}

export const useTenants = () => {
  const result: TenantContextType = React.useContext(TenantContext);
  return result;
}

export const useAssignees = (row: { assignees: UserId[] }) => {
  const org = useOrg();
  const { assignees } = row;
  const [searchString, setSearchString] = React.useState<string>('');
  const searchResults = React.useMemo(() => org.state.findUsers(searchString, assignees), [assignees, searchString, org]);
  return { searchString, setSearchString, searchResults };
}

export const useProjectUsers = (row: { assignees: UserId[] }) => {
  const org = useOrg();
  const [searchString, setSearchString] = React.useState<string>('');
  const searchResults = React.useMemo(() => org.state.findProjectUsers(searchString, row.assignees), [row, searchString, org]);
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

export const useTenantConfig = () => {
  const result: TenantConfigContextType = React.useContext(TenantConfigContext);
  return result;
}

export const useApp = () => {
  const apps = Burger.useApps();

  function changeApp(input: 'tasks' | 'frontoffice' | 'stencil' | RepoType) {
    if (input === 'frontoffice') {
      apps.actions.handleActive('app-frontoffice');
      return;
    }

    if (input === 'tasks' || input === 'TASKS') {
      apps.actions.handleActive('app-tasks');
      return;
    }

    if (input === 'stencil' || input === 'STENCIL') {
      apps.actions.handleActive('app-stencil');
      return;
    }
  }


  return { changeApp };
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