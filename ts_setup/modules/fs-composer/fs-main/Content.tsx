import React from 'react';
import { useIntl } from 'react-intl';
import { FsTab } from '@dxs-ts/fs-api';
import { OwnerState } from './useOwnerState';
import { FsDirentCreateArticle } from '../fs-dirent-create-article';

export interface ContentProps {
  ownerState: OwnerState;
  children: React.ReactNode;
  className: string;
}

function getTabKey(tab: FsTab | undefined): string {
  if (!tab) {
    return 'none';
  }
  if (tab.type === 'edit') {
    return 'edit';
  }
  return `create.${tab.direntType}`;
}

export const Content: React.FC<ContentProps> = ({ className, ownerState, children }) => {
  const intl = useIntl();
  const activeTab = ownerState.openTabs[ownerState.activeTabIndex];
  const tabKey = getTabKey(activeTab);
  const parentFolder = activeTab?.type === 'create' ? activeTab.parentFolder : undefined;

  switch (tabKey) {
    case 'create.article': {
      return (
        <div className={className}>
          <FsDirentCreateArticle parentFolder={parentFolder} />
        </div>
      );
    }
    case 'edit': {
      return (
        <div className={className}>
          {children}
        </div>
      );
    }
    default: {
      return (
        <div className={className}>
          {intl.formatMessage({ id: 'fs.main.message.noAssetSelected' })}
        </div>
      );
    }
  }
};

