import React from 'react';
import { useFsDirent, Fs } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';


export interface AssetCount {
  type: Fs.BodyType;
  count: number;
}

export interface DanglingGroup {
  label: string;
  descriptionId: string;
  items: string[] | 'TODO';
}

export interface DisabledAsset {
  fullPath: string;
  type: Fs.BodyType;
}

export interface OwnerState {
  isDarkMode: boolean;
  assetCounts: AssetCount[];
  danglingGroups: DanglingGroup[];
  disabledAssets: DisabledAsset[];
  overviewExpanded: boolean;
  danglingExpanded: boolean;
  disabledExpanded: boolean;
  onToggleOverview: () => void;
  onToggleDangling: () => void;
  onToggleDisabled: () => void;
}

export const useOwnerState = (): OwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent, selectOptions, getDirentName } = useFsDirent();
  const [overviewExpanded, setOverviewExpanded] = React.useState(false);
  const [danglingExpanded, setDanglingExpanded] = React.useState(false);
  const [disabledExpanded, setDisabledExpanded] = React.useState(false);

  function onToggleOverview() {
    setOverviewExpanded(prev => !prev);
  }
  function onToggleDangling() {
    setDanglingExpanded(prev => !prev);
  }
  function onToggleDisabled() {
    setDisabledExpanded(prev => !prev);
  }

  const allProps = Object.values(selectOptions.direntProps);

  // Section 1: Asset counts by type
  const countsByType: Partial<Record<Fs.BodyType, number>> = {};
  allProps.forEach(p => {
    countsByType[p.type] = (countsByType[p.type] ?? 0) + 1;
  });
  const assetCounts: AssetCount[] = Object.entries(countsByType)
    .map(([type, count]) => ({ type: type as Fs.BodyType, count: count! }))
    .sort((a, b) => a.type.localeCompare(b.type));

  // Section 2: Dangling assets
  const pages = allProps.filter(p => p.type === 'ARTICLE_PAGE') as Fs.PageProps[];
  const pageArticleIds = pages.map(p => p.articleId);

  const workflows = allProps.filter(p => p.type === 'ARTICLE_WORKFLOW') as Fs.WorkflowProps[];
  const workflowFlowNames = workflows.map(w => w.flowName);

  const danglingArticles = allProps
    .filter(p => p.type === 'ARTICLE' && !pageArticleIds.includes(p.id))
    .map(p => getDirentName(p.id) ?? p.id);

  const danglingWorkflows = workflows
    .filter(w => w.articles.length === 0)
    .map(w => getDirent(w.id)?.name ?? w.id);

  const danglingLinks = (allProps.filter(p => p.type === 'ARTICLE_LINK') as Fs.LinkProps[])
    .filter(l => l.articles.length === 0)
    .map(l => getDirent(l.id)?.name ?? l.id);

  const danglingFlows = (allProps.filter(p => p.type === 'FLOW') as Fs.FlowProps[])
    .filter(f => !workflowFlowNames.includes(f.name))
    .map(f => getDirent(f.id)?.name ?? f.id);

  const danglingFolders = allProps
    .filter(p => p.type === 'FOLDER')
    .map(p => getDirent(p.id))
    .filter((d): d is Fs.DirentBase => d !== undefined && d.children.length === 0)
    .map(d => d.name);

  const workflowFormNames = workflows.map(w => w.dialobFormName);
  const danglingForms = (allProps.filter(p => p.type === 'DIALOB_FORM') as Fs.DialobProps[])
    .filter(f => !workflowFormNames.includes(f.formName))
    .map(f => getDirent(f.id)?.name ?? f.id);

  const danglingGroups: DanglingGroup[] = [
    { label: 'Articles',        descriptionId: 'fs.direntStats.dangling.articles.desc',       items: danglingArticles },
    { label: 'Workflows',       descriptionId: 'fs.direntStats.dangling.workflows.desc',      items: danglingWorkflows },
    { label: 'Links',           descriptionId: 'fs.direntStats.dangling.links.desc',          items: danglingLinks },
    { label: 'Flows',           descriptionId: 'fs.direntStats.dangling.flows.desc',          items: danglingFlows },
    { label: 'Folders',         descriptionId: 'fs.direntStats.dangling.folders.desc',        items: danglingFolders },
    { label: 'Flow Tasks',      descriptionId: 'fs.direntStats.dangling.flowTasks.desc',      items: 'TODO' },
    { label: 'Decision Tables', descriptionId: 'fs.direntStats.dangling.decisionTables.desc', items: 'TODO' },
    { label: 'Forms',           descriptionId: 'fs.direntStats.dangling.forms.desc',          items: danglingForms },
    { label: 'Printouts',       descriptionId: 'fs.direntStats.dangling.printouts.desc',      items: 'TODO' },
  ];

  // Section 3: Disabled assets (configOption: disabledMode)
  const disabledAssets: DisabledAsset[] = allProps
    .filter(p => p.configOptions?.includes('DISABLED_MODE'))
    .map(p => ({ fullPath: getDirent(p.id)?.fullPath ?? p.id, type: p.type }));

  return {
    isDarkMode,
    assetCounts,
    danglingGroups,
    disabledAssets,
    overviewExpanded,
    danglingExpanded,
    disabledExpanded,
    onToggleOverview,
    onToggleDangling,
    onToggleDisabled,
  };
};
