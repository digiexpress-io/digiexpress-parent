import React from 'react';
import { useIntl } from 'react-intl';
import { FsTab } from '@dxs-ts/fs-api';
import { OwnerState } from './useOwnerState';
import { FsDirentCreateArticle } from '../fs-dirent-create-article';
import { FsDirentCreateFolder } from '../fs-dirent-create-folder';
import { FsDirentCreateLink } from '../fs-dirent-create-link';
import { FsDirentCreatePhone } from '../fs-dirent-create-phone';
import { FsDirentCreateService } from '../fs-dirent-create-service';
import { FsDirentCreateLanguage } from '../fs-dirent-create-language';
import { FsDirentCreateDialob } from '../fs-dirent-create-dialob';

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
  const pathToTopParent = activeTab?.type === 'create' ? activeTab.pathToTopParent : undefined;

  switch (tabKey) {
    case 'create.folder': {
      return (
        <div className={className}>
          <FsDirentCreateFolder parentFolder={parentFolder} pathToTopParent={pathToTopParent} />
        </div>
      );
    }
    case 'create.link': {
      return (
        <div className={className}>
          <FsDirentCreateLink parentFolder={parentFolder} pathToTopParent={pathToTopParent} />
        </div>
      );
    }
    case 'create.phone': {
      return (
        <div className={className}>
          <FsDirentCreatePhone parentFolder={parentFolder} pathToTopParent={pathToTopParent} />
        </div>
      );
    }
    case 'create.article': {
      return (
        <div className={className}>
          <FsDirentCreateArticle parentFolder={parentFolder} pathToTopParent={pathToTopParent} />
        </div>
      );
    }
    case 'create.service': {
      return (
        <div className={className}>
          <FsDirentCreateService parentFolder={parentFolder} pathToTopParent={pathToTopParent} />
        </div>
      );
    }
    case 'create.dialob': {
      return (
        <div className={className}>
          <FsDirentCreateDialob parentFolder={parentFolder} pathToTopParent={pathToTopParent} />
        </div>
      );
    }
    case 'create.language': {
      return (
        <div className={className}>
          <FsDirentCreateLanguage parentFolder={parentFolder} pathToTopParent={pathToTopParent} />
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

