import React from 'react';
import { FsNode, FsNodeSecondaryView, FsOpenTab, useFs } from '@dxs-ts/fs-api';
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
  activeNode: FsNode | undefined;
  isRightPanelOpen: boolean;
  selectedView: FsNodeSecondaryView | undefined;
  activeTabIndex: number;
  openTabs: FsOpenTab[];
  toggleRightPanel: () => void;
  handleViewChange: (view: FsNodeSecondaryView) => void;
  toolbar: {
    width: string;
    buttons: PanelButton[];
  };
}

const toolbarWidth = '50px';

export const useOwnerState = (_props: FsMainProps): OwnerState => {
  const { isDarkMode, activeNode } = useFs();
  const [isRightPanelOpen, setIsRightPanelOpen] = React.useState(true);
  const [selectedView, setSelectedView] = React.useState<FsNodeSecondaryView | undefined>();
  const { activeTabIndex, openTabs } = useFs();

  const toggleRightPanel = React.useCallback(() => {
    setIsRightPanelOpen(prev => !prev);
  }, []);

  const handleViewChange = React.useCallback((view: FsNodeSecondaryView) => {
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
      tooltip: isRightPanelOpen ? 'Collapse Panel' : 'Expand Panel',
      isSelected: false,
      onClick: toggleRightPanel,
    },
    {
      id: 'properties',
      type: 'view',
      icon: FsIcons.Info,
      tooltip: 'Properties',
      isSelected: selectedView === 'properties',
      onClick: () => handleViewChange('properties'),
    },
    {
      id: 'configuration',
      type: 'view',
      icon: FsIcons.Settings,
      tooltip: 'Configuration',
      isSelected: selectedView === 'configuration',
      onClick: () => handleViewChange('configuration'),
    },
    {
      id: 'references',
      type: 'view',
      icon: FsIcons.Tree,
      tooltip: 'References',
      isSelected: selectedView === 'references',
      onClick: () => handleViewChange('references'),
    },
    {
      id: 'debug',
      type: 'view',
      icon: FsIcons.Debug,
      tooltip: 'Debug',
      isSelected: selectedView === 'debug',
      onClick: () => handleViewChange('debug'),
    },
    {
      id: 'errors',
      type: 'view',
      icon: FsIcons.Error,
      tooltip: 'Errors',
      isSelected: selectedView === 'errors',
      onClick: () => handleViewChange('errors'),
    },
    {
      id: 'preview',
      type: 'view',
      icon: FsIcons.Preview,
      tooltip: 'Preview',
      isSelected: selectedView === 'preview',
      onClick: () => handleViewChange('preview'),
    },
    {
      id: 'history',
      type: 'view',
      icon: FsIcons.History,
      tooltip: 'History',
      isSelected: selectedView === 'history',
      onClick: () => handleViewChange('history'),
    },
    {
      id: 'help',
      type: 'view',
      icon: FsIcons.Help,
      tooltip: 'Help',
      isSelected: selectedView === 'help',
      onClick: () => handleViewChange('help'),
    },
    {
      id: 'changes',
      type: 'save',
      icon: FsIcons.Save,
      tooltip: 'Save',
      isSelected: selectedView === 'changes',
      badge: 7,
      onClick: () => handleViewChange('changes'),
    },
  ], [isRightPanelOpen, selectedView, toggleRightPanel, handleViewChange]);

  return {
    isDarkMode,
    activeNode,
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