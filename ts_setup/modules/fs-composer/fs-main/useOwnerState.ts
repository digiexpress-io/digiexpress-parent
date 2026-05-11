import React from 'react';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { FsTab, useFsNav } from '@dxs-ts/fs-nav';
import { useFsTheme } from '../fs-theme';
import { FsIcons } from '../fs-theme/fs-icons';
import { FsMainProps } from './FsMainProps';

type ToolbarButtonType = 'toggle' | 'view' | 'save';
type ViewSupport = Partial<Record<Fs.BodyType, Fs.SecondaryView[]>>;

const SUPPORTED_VIEWS: ViewSupport = {
  FOLDER: ['properties', 'changes', 'overview', 'article-order', 'article-locale-overview', 'stats'],
  ARTICLE: ['properties', 'references', 'history', 'changes', 'article-order', 'overview', 'article-locale-overview', 'stats'],
  ARTICLE_PAGE: ['properties', 'preview', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats'],
  ARTICLE_TEMPLATE: ['properties', 'preview', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats'],
  ARTICLE_WORKFLOW: ['properties', 'references', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats'],
  ARTICLE_LINK: ['properties', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats'],
  FLOW: ['properties', 'references', 'debug', 'errors', 'preview', 'history', 'changes', 'article-order', 'article-locale-overview', 'debug', 'stats'],
  FLOW_TASK: ['properties', 'references', 'debug', 'errors', 'history', 'changes', 'article-order', 'article-locale-overview', 'debug', 'stats'],
  DECISION_TABLE: ['properties', 'references', 'errors', 'history', 'changes', 'article-order', 'article-locale-overview', 'debug', 'stats'],
  DIALOB_FORM: ['properties', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats'],
  PRINTOUT: ['properties', 'references', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats'],
  PRINTOUT_PAGE: ['properties', 'preview', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats'],
  PRINTOUT_RESOURCE: ['properties', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats'],
  LOCALE: ['properties', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats'],
  DEPLOYMENT: ['properties', 'history', 'changes', 'article-order', 'article-locale-overview', 'stats'],
};

export interface PanelButton {
  id: string;
  type: ToolbarButtonType;
  icon: React.ElementType;
  tooltip: string;
  isSelected: boolean;
  isEnabled: boolean;
  badge?: number;
  onClick: () => void;
}

export interface OwnerState {
  isDarkMode: boolean;
  activeDirent: Fs.DirentBase | undefined;
  isRightPanelOpen: boolean;
  selectedView: Fs.SecondaryView | undefined;
  activeTabIndex: number;
  openTabs: FsTab[];
  toggleRightPanel: () => void;
  handleViewChange: (view: Fs.SecondaryView) => void;
  toolbar: {
    width: string;
    buttons: PanelButton[];
  };
}

const toolbarWidth = '50px';

export const useOwnerState = (_props: FsMainProps): OwnerState => {
  const intl = useIntl();
  const { isDarkMode } = useFsTheme();
  const { activeDirent, activeTabIndex, openTabs } = useFsNav();
  const { getDirent } = useFsDirent();
  const activeDirentEntry = activeDirent ? getDirent(activeDirent.id) : undefined;
  const [isRightPanelOpen, setIsRightPanelOpen] = React.useState(true);
  const [selectedView, setSelectedView] = React.useState<Fs.SecondaryView | undefined>();

  React.useEffect(() => {
    if (activeDirent) {
      setSelectedView(activeDirent.type === 'ARTICLE_PAGE' || activeDirent.type === 'FLOW' || activeDirent.type === 'ARTICLE_TEMPLATE' ? 'preview' : 'properties');
    }
  }, [activeDirent?.id]);

  const toggleRightPanel = React.useCallback(() => {
    setIsRightPanelOpen(prev => !prev);
  }, []);

  const handleViewChange = React.useCallback((view: Fs.SecondaryView) => {
    setSelectedView(view);
    if (!isRightPanelOpen) {
      setIsRightPanelOpen(true);
    }
  }, [isRightPanelOpen]);

  const supportedViews = activeDirentEntry?.type ? (SUPPORTED_VIEWS[activeDirentEntry.type] ?? []) : [];
  const isViewEnabled = (view: Fs.SecondaryView) => supportedViews.includes(view);

  const toolbarButtons = React.useMemo((): PanelButton[] => [
    {
      id: 'toggle-panel',
      type: 'toggle',
      icon: isRightPanelOpen ? FsIcons.CollapseAll : FsIcons.ExpandAll,
      tooltip: isRightPanelOpen ? intl.formatMessage({ id: 'fs.main.tooltip.togglePanelCollapse' }) : intl.formatMessage({ id: 'fs.main.tooltip.togglePanelExpand' }),
      isSelected: false,
      isEnabled: true,
      onClick: toggleRightPanel,
    },
    {
      id: 'properties',
      type: 'view',
      icon: FsIcons.Info,
      tooltip: intl.formatMessage({ id: 'fs.main.tooltip.properties' }),
      isSelected: selectedView === 'properties',
      isEnabled: isViewEnabled('properties'),
      onClick: () => handleViewChange('properties'),
    },
    {
      id: 'references',
      type: 'view',
      icon: FsIcons.Tree,
      tooltip: intl.formatMessage({ id: 'fs.main.tooltip.references' }),
      isSelected: selectedView === 'references',
      isEnabled: isViewEnabled('references'),
      onClick: () => handleViewChange('references'),
    },
    {
      id: 'debug',
      type: 'view',
      icon: FsIcons.Debug,
      tooltip: intl.formatMessage({ id: 'fs.main.tooltip.debug' }),
      isSelected: selectedView === 'debug',
      isEnabled: isViewEnabled('debug'),
      onClick: () => handleViewChange('debug'),
    },
    {
      id: 'errors',
      type: 'view',
      icon: FsIcons.Error,
      tooltip: intl.formatMessage({ id: 'fs.main.tooltip.errors' }),
      isSelected: selectedView === 'errors',
      isEnabled: isViewEnabled('errors'),
      onClick: () => handleViewChange('errors'),
    },
    {
      id: 'preview',
      type: 'view',
      icon: FsIcons.Preview,
      tooltip: intl.formatMessage({ id: 'fs.main.tooltip.preview' }),
      isSelected: selectedView === 'preview',
      isEnabled: isViewEnabled('preview'),
      onClick: () => handleViewChange('preview'),
    },
    {
      id: 'history',
      type: 'view',
      icon: FsIcons.History,
      tooltip: intl.formatMessage({ id: 'fs.main.tooltip.history' }),
      isSelected: selectedView === 'history',
      isEnabled: isViewEnabled('history'),
      onClick: () => handleViewChange('history'),
    },
    {
      id: 'help',
      type: 'view',
      icon: FsIcons.Help,
      tooltip: intl.formatMessage({ id: 'fs.main.tooltip.help' }),
      isSelected: selectedView === 'help',
      isEnabled: isViewEnabled('help'),
      onClick: () => handleViewChange('help'),
    },
    {
      id: 'article-order',
      type: 'view',
      icon: FsIcons.ArticleOrder,
      tooltip: intl.formatMessage({ id: 'fs.main.tooltip.articleOrder' }),
      isSelected: selectedView === 'article-order',
      isEnabled: isViewEnabled('article-order'),
      onClick: () => handleViewChange('article-order'),
    },
    {
      id: 'overview',
      type: 'view',
      icon: FsIcons.Tree,
      tooltip: intl.formatMessage({ id: 'fs.main.tooltip.overview' }),
      isSelected: selectedView === 'overview',
      isEnabled: isViewEnabled('overview'),
      onClick: () => handleViewChange('overview'),
    },
    {
      id: 'article-locale-overview',
      type: 'view',
      icon: FsIcons.Language,
      tooltip: intl.formatMessage({ id: 'fs.main.tooltip.articleLocaleOverview' }),
      isSelected: selectedView === 'article-locale-overview',
      isEnabled: isViewEnabled('article-locale-overview'),
      onClick: () => handleViewChange('article-locale-overview'),
    },
    {
      id: 'stats',
      type: 'view',
      icon: FsIcons.Stats,
      tooltip: intl.formatMessage({ id: 'fs.main.tooltip.stats' }),
      isSelected: selectedView === 'stats',
      isEnabled: isViewEnabled('stats'),
      onClick: () => handleViewChange('stats'),
    },
    {
      id: 'changes',
      type: 'save',
      icon: FsIcons.Save,
      tooltip: intl.formatMessage({ id: 'fs.main.tooltip.changes' }),
      isSelected: selectedView === 'changes',
      isEnabled: true,
      badge: 7,
      onClick: () => handleViewChange('changes'),
    },
  ], [isRightPanelOpen, selectedView, toggleRightPanel, handleViewChange, supportedViews]);

  return {
    isDarkMode,
    activeDirent: activeDirentEntry,
    activeTabIndex,
    openTabs,
    isRightPanelOpen,
    selectedView,
    toggleRightPanel,
    handleViewChange,
    toolbar: {
      width: toolbarWidth,
      buttons: toolbarButtons,
    },
  };
};