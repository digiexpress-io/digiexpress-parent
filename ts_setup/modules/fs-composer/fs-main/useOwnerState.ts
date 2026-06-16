import React from 'react';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { FsTab, useFsNav } from '@dxs-ts/fs-nav';
import { useFsTheme } from '../fs-theme';
import { createWidget, DirentWidget } from '../fs-factory';
import { FsMainProps } from './FsMainProps';


export interface OwnerState {
  isDarkMode: boolean;
  activeDirent: Fs.DirentBase | undefined;
  isRightPanelOpen: boolean;
  selectedView: Fs.SecondaryView | undefined;
  activeTab: FsTab | undefined;
  activeTabId: number | undefined;
  activeWidget: DirentWidget | undefined;
  openTabs: FsTab[];


  toggleRightPanel: () => void;
  onViewChange: (view: Fs.SecondaryView) => void;
}


export const useOwnerState = (_props: FsMainProps): OwnerState => {

  const { isDarkMode } = useFsTheme();
  const { activeDirent, activeTabIndex: activeTabId, openTabs } = useFsNav();
  const { getDirent } = useFsDirent();
  const activeTab = openTabs[activeTabId];
  const activeDirentEntry = activeDirent ? getDirent(activeDirent.id) : undefined;
  const activeDirentType = activeDirentEntry?.type ?? (activeTab?.type === 'create' ? activeTab?.direntType : undefined);
  const activeWidget = activeDirentType ? createWidget({ type: activeDirentType }) : undefined;


  const [isRightPanelOpen, setIsRightPanelOpen] = React.useState(false);
  const [selectedView, setSelectedView] = React.useState<Fs.SecondaryView | undefined>();


  React.useEffect(() => {
    if (activeDirent) {
      setSelectedView(activeDirent.type === 'ARTICLE_PAGE' || activeDirent.type === 'FLOW' || activeDirent.type === 'ARTICLE_TEMPLATE' ? 'preview' : 'properties');
    } else {
      setIsRightPanelOpen(false);
      setSelectedView(undefined);
    }
  }, [activeDirent?.id]);

  const toggleRightPanel = React.useCallback(() => {
    setIsRightPanelOpen(prev => !prev);
  }, []);

  const onViewChange = React.useCallback((view: Fs.SecondaryView) => {
    if (isRightPanelOpen && selectedView === view) {
      setIsRightPanelOpen(false);
    } else {
      setSelectedView(view);
      setIsRightPanelOpen(true);
    }
  }, [isRightPanelOpen, selectedView]);

  return {
    isDarkMode,
    activeDirent: activeDirentEntry,
    activeTab,
    openTabs,
    isRightPanelOpen,
    selectedView,
    activeTabId,
    activeWidget,
    toggleRightPanel,
    onViewChange
  };
};