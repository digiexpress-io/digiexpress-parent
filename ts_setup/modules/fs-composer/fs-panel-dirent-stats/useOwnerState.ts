import { useFsDirent, Fs } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { FsPanelDirentStatsProps } from './FsPanelDirentStatsProps';


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
  name: string;
  type: Fs.BodyType;
}

export interface OwnerState {
  isDarkMode: boolean;
  assetCounts: AssetCount[];
  danglingGroups: DanglingGroup[];
  disabledAssets: DisabledAsset[];
}

export const useOwnerState = (_props: FsPanelDirentStatsProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent, selectOptions, getArticleName } = useFsDirent();

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
  const articleIdsWithPages = new Set(pages.map(p => p.articleId));

  const workflows = allProps.filter(p => p.type === 'ARTICLE_WORKFLOW') as Fs.WorkflowProps[];
  const flowNamesInWorkflows = new Set(workflows.map(w => w.flowName));

  const danglingArticles = allProps
    .filter(p => p.type === 'ARTICLE' && !articleIdsWithPages.has(p.id))
    .map(p => getArticleName(p.id) ?? p.id);

  const danglingWorkflows = workflows
    .filter(w => w.articles.length === 0)
    .map(w => getDirent(w.id)?.name ?? w.id);

  const danglingLinks = (allProps.filter(p => p.type === 'ARTICLE_LINK') as Fs.LinkProps[])
    .filter(l => l.articles.length === 0)
    .map(l => getDirent(l.id)?.name ?? l.id);

  const danglingFlows = (allProps.filter(p => p.type === 'FLOW') as Fs.FlowProps[])
    .filter(f => !flowNamesInWorkflows.has(f.name))
    .map(f => getDirent(f.id)?.name ?? f.id);

  const danglingFolders = allProps
    .filter(p => p.type === 'FOLDER')
    .map(p => getDirent(p.id))
    .filter((d): d is Fs.DirentBase => d !== undefined && d.children.length === 0)
    .map(d => d.name);

  const danglingGroups: DanglingGroup[] = [
    { label: 'Articles',        descriptionId: 'fs.direntStats.dangling.articles.desc',       items: danglingArticles },
    { label: 'Workflows',       descriptionId: 'fs.direntStats.dangling.workflows.desc',      items: danglingWorkflows },
    { label: 'Links',           descriptionId: 'fs.direntStats.dangling.links.desc',          items: danglingLinks },
    { label: 'Flows',           descriptionId: 'fs.direntStats.dangling.flows.desc',          items: danglingFlows },
    { label: 'Folders',         descriptionId: 'fs.direntStats.dangling.folders.desc',        items: danglingFolders },
    { label: 'Flow Tasks',      descriptionId: 'fs.direntStats.dangling.flowTasks.desc',      items: 'TODO' },
    { label: 'Decision Tables', descriptionId: 'fs.direntStats.dangling.decisionTables.desc', items: 'TODO' },
    { label: 'Forms',           descriptionId: 'fs.direntStats.dangling.forms.desc',          items: 'TODO' },
    { label: 'Printouts',       descriptionId: 'fs.direntStats.dangling.printouts.desc',      items: 'TODO' },
  ];

  // Section 3: Disabled assets (configOption: disabledMode)
  const disabledAssets: DisabledAsset[] = allProps
    .filter(p => p.configOptions?.includes('disabledMode'))
    .map(p => ({ name: getDirent(p.id)?.name ?? p.id, type: p.type }));

  return { isDarkMode, assetCounts, danglingGroups, disabledAssets };
};
