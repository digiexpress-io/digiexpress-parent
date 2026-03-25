import React from 'react';
import { useIntl } from 'react-intl';
import { FsTab } from '@dxs-ts/fs-api';
import { OwnerState } from './useOwnerState';
import { FsDirentArticle } from '../fs-dirent-article';
import { FsDirentCreateFolder } from '../fs-dirent-create-folder';
import { FsDirentCreateLink } from '../fs-dirent-create-link';
import { FsDirentCreatePhone } from '../fs-dirent-create-phone';
import { FsDirentCreateService } from '../fs-dirent-create-service';
import { FsDirentCreateLanguage } from '../fs-dirent-create-language';
import { FsDirentCreateDialob } from '../fs-dirent-create-dialob';
import { FsDirentCreatePrintout } from '../fs-dirent-create-printout';

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
    return tab.dirent.type;
  }
  return tab.direntType;
}

export const Content: React.FC<ContentProps> = ({ className, ownerState }) => {
  const intl = useIntl();
  const activeTab = ownerState.openTabs[ownerState.activeTabIndex];
  const tabKey = getTabKey(activeTab);
  const parentFolder = activeTab?.type === 'create' ? activeTab.parentFolder : undefined;
  const pathToTopParent = activeTab?.type === 'create' ? activeTab.pathToTopParent : undefined;

  switch (tabKey) {
    case 'article': return (activeTab && (<div className={className}><FsDirentArticle tab={activeTab} /></div>));
    case 'folder': return (activeTab && (<div className={className}><FsDirentCreateFolder parentFolder={parentFolder} pathToTopParent={pathToTopParent} /></div>));
    case 'link': return (activeTab && (<div className={className}><FsDirentCreateLink parentFolder={parentFolder} pathToTopParent={pathToTopParent} /></div>));
    case 'phone': return (activeTab && (<div className={className}><FsDirentCreatePhone parentFolder={parentFolder} pathToTopParent={pathToTopParent} /></div>));
    case 'service': return (activeTab && (<div className={className}><FsDirentCreateService parentFolder={parentFolder} pathToTopParent={pathToTopParent} /></div>));
    case 'printout': return (activeTab && (<div className={className}><FsDirentCreatePrintout parentFolder={parentFolder} pathToTopParent={pathToTopParent} /></div>));
    case 'dialob': return (activeTab && (<div className={className}><FsDirentCreateDialob parentFolder={parentFolder} pathToTopParent={pathToTopParent} /></div>));
    case 'language': return (activeTab && (<div className={className}><FsDirentCreateLanguage parentFolder={parentFolder} pathToTopParent={pathToTopParent} /></div>));
    default: return (<div className={className}>{intl.formatMessage({ id: 'fs.main.message.noAssetSelected' })}</div>);
  }
};

