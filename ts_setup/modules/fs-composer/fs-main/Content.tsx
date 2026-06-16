import React from 'react';
import { FsMainDefaultBackground } from './FsMainDefaultBackground';
import { FsTab } from '@dxs-ts/fs-nav';
import { OwnerState } from './useOwnerState';
import { createWidget } from '../fs-factory';
import { useFsDirent } from '@dxs-ts/fs-api';


export interface ContentProps {
  ownerState: OwnerState;
  children: React.ReactNode;
  className: string;
}

function resolveTab(tab: FsTab | undefined): { tabType: string; tabId: string } {
  if (!tab) {
    return {
      tabType: 'none',
      tabId: 'none',
    };
  }
  if (tab.type === 'edit') {
    return {
      tabType: tab.dirent.type,
      tabId: tab.dirent.id,
    };
  }
  return {
    tabType: tab.direntType,
    tabId: tab.direntType,
  };
}

export const Content: React.FC<ContentProps> = ({ className, ownerState }) => {
  const activeTab = ownerState.openTabs[ownerState.activeTabIndex];
  const { tabId } = resolveTab(activeTab);
  const { getDirent } = useFsDirent();
  const dirent = getDirent(tabId);


  // some create view
  if (!dirent && tabId && activeTab?.type === 'create') {
    const widget = createWidget({ type: tabId as any });
    return (
      <div key={tabId} className={className}>
        <widget.views.CreateView />
      </div>);
  }

  if (dirent) {
    const widget = createWidget(dirent);
    return (
      <div key={tabId} className={className}>
        <widget.views.UpdateView direntId={dirent.id} />
      </div>
    )
  }

  return (
    <div key={tabId} className={className}>
      <FsMainDefaultBackground />
    </div>);
}