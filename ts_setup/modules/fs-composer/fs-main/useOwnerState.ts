import React from 'react';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { FsTab, useFsNav } from '@dxs-ts/fs-nav';
import { createWidget, DirentWidget } from '../fs-factory';


export interface OwnerState {
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


export const useOwnerState = (): OwnerState => {
  const { activeDirent, activeTabIndex: activeTabId, openTabs } = useFsNav();
  const { getDirent } = useFsDirent();
  const activeTab = openTabs[activeTabId];
  const activeDirentEntry = activeDirent ? getDirent(activeDirent.id) : undefined;
  const activeDirentType = activeDirentEntry?.type ?? (activeTab?.type === 'create' ? activeTab?.direntType : undefined);
  const activeWidget = activeDirentType ? createWidget({ type: activeDirentType }) : undefined;

  const autoPreview = activeDirent?.type === 'ARTICLE_PAGE' || activeDirent?.type === 'FLOW' || activeDirent?.type === 'PRINTOUT_PAGE';

  const [isRightPanelOpen, setIsRightPanelOpen] = React.useState(false);
  const [selectedView, setSelectedView] = React.useState<Fs.SecondaryView | undefined>();


  React.useEffect(() => {
    if (activeDirent) {
      setSelectedView(autoPreview ? 'preview' : 'properties');
      setIsRightPanelOpen(autoPreview);
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