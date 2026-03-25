import React from 'react';
import { useIntl } from 'react-intl';
import { FsDirentEntry, FsDirentSecondaryView, FsTab, useFsNav, useFsDirentProps } from '@dxs-ts/fs-api';
import { FsIcons } from '../fs-theme/fs-icons';
import { FsMainProps } from './FsMainProps';

type ToolbarButtonType = 'toggle' | 'view' | 'save';

export interface PanelButton {
  id: string;
  type: ToolbarButtonType;
  icon: React.ElementType;
  tooltip: string;
  isSelected: boolean;
  badge?: number;
  onClick: () => void;
}

export interface OwnerState {
  isDarkMode: boolean;
  activeDirent: FsDirentEntry | undefined;
  isRightPanelOpen: boolean;
  selectedView: FsDirentSecondaryView | undefined;
  activeTabIndex: number;
  openTabs: FsTab[];
  toggleRightPanel: () => void;
  handleViewChange: (view: FsDirentSecondaryView) => void;
  toolbar: {
    width: string;
    buttons: PanelButton[];
  };
}

const toolbarWidth = '50px';

export const useOwnerState = (_props: FsMainProps): OwnerState => {
  const intl = useIntl();
  const { isDarkMode, activeDirent, activeTabIndex, openTabs } = useFsNav();
  const { getDirent } = useFsDirentProps();
  const activeDirentEntry = activeDirent ? getDirent(activeDirent.id) : undefined;
  const [isRightPanelOpen, setIsRightPanelOpen] = React.useState(true);
  const [selectedView, setSelectedView] = React.useState<FsDirentSecondaryView | undefined>();

  React.useEffect(() => {
    if (activeDirent) {
      setSelectedView('properties');
    }
  }, [activeDirent?.id]);

  const toggleRightPanel = React.useCallback(() => {
    setIsRightPanelOpen(prev => !prev);
  }, []);

  const handleViewChange = React.useCallback((view: FsDirentSecondaryView) => {
    setSelectedView(view);
    if (!isRightPanelOpen) {
      setIsRightPanelOpen(true);
    }
  }, [isRightPanelOpen]);

  const toolbarButtons = React.useMemo((): PanelButton[] => [
    {
      id: 'toggle-panel',
      type: 'toggle',
      icon: isRightPanelOpen ? FsIcons.CollapseAll : FsIcons.ExpandAll,
      tooltip: isRightPanelOpen ? intl.formatMessage({ id: 'fs.main.tooltip.togglePanelCollapse' }) : intl.formatMessage({ id: 'fs.main.tooltip.togglePanelExpand' }),
      isSelected: false,
      onClick: toggleRightPanel,
    },
    {
      id: 'properties',
      type: 'view',
      icon: FsIcons.Info,
      tooltip: intl.formatMessage({ id: 'fs.main.tooltip.properties' }),
      isSelected: selectedView === 'properties',
      onClick: () => handleViewChange('properties'),
    },
    {
      id: 'configuration',
      type: 'view',
      icon: FsIcons.Settings,
      tooltip: intl.formatMessage({ id: 'fs.main.tooltip.configuration' }),
      isSelected: selectedView === 'configuration',
      onClick: () => handleViewChange('configuration'),
    },
    {
      id: 'references',
      type: 'view',
      icon: FsIcons.Tree,
      tooltip: intl.formatMessage({ id: 'fs.main.tooltip.references' }),
      isSelected: selectedView === 'references',
      onClick: () => handleViewChange('references'),
    },
    {
      id: 'debug',
      type: 'view',
      icon: FsIcons.Debug,
      tooltip: intl.formatMessage({ id: 'fs.main.tooltip.debug' }),
      isSelected: selectedView === 'debug',
      onClick: () => handleViewChange('debug'),
    },
    {
      id: 'errors',
      type: 'view',
      icon: FsIcons.Error,
      tooltip: intl.formatMessage({ id: 'fs.main.tooltip.errors' }),
      isSelected: selectedView === 'errors',
      onClick: () => handleViewChange('errors'),
    },
    {
      id: 'preview',
      type: 'view',
      icon: FsIcons.Preview,
      tooltip: intl.formatMessage({ id: 'fs.main.tooltip.preview' }),
      isSelected: selectedView === 'preview',
      onClick: () => handleViewChange('preview'),
    },
    {
      id: 'history',
      type: 'view',
      icon: FsIcons.History,
      tooltip: intl.formatMessage({ id: 'fs.main.tooltip.history' }),
      isSelected: selectedView === 'history',
      onClick: () => handleViewChange('history'),
    },
    {
      id: 'help',
      type: 'view',
      icon: FsIcons.Help,
      tooltip: intl.formatMessage({ id: 'fs.main.tooltip.help' }),
      isSelected: selectedView === 'help',
      onClick: () => handleViewChange('help'),
    },
    {
      id: 'article-order',
      type: 'view',
      icon: FsIcons.ArticleOrder,
      tooltip: intl.formatMessage({ id: 'fs.main.tooltip.articleOrder' }),
      isSelected: selectedView === 'article-order',
      onClick: () => handleViewChange('article-order'),
    },
    {
      id: 'changes',
      type: 'save',
      icon: FsIcons.Save,
      tooltip: intl.formatMessage({ id: 'fs.main.tooltip.changes' }),
      isSelected: selectedView === 'changes',
      badge: 7,
      onClick: () => handleViewChange('changes'),
    },
  ], [isRightPanelOpen, selectedView, toggleRightPanel, handleViewChange]);

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