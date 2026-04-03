import React from 'react';
import { useIntl } from 'react-intl';
import { FsTab } from '@dxs-ts/fs-api';
import { OwnerState } from './useOwnerState';
import { FsDirentArticle } from '../fs-dirent-article';
import { FsDirentFolder } from '../fs-dirent-folder';
import { FsDirentLink } from '../fs-dirent-link';
import { FsDirentPhone } from '../fs-dirent-phone';
import { FsDirentService } from '../fs-dirent-service';
import { FsDirentLanguage } from '../fs-dirent-language';
import { FsDirentPage } from '../fs-dirent-page';
import { FsDirentFlow } from '../fs-dirent-flow';
import { FsDirentDialob } from '../fs-dirent-dialob';
import { FsDirentPrintout } from '../fs-dirent-printout';
import { FsDirentTemplate } from '../fs-dirent-template';


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

  switch (tabKey) {
    case 'article': return (activeTab && (<div className={className}><FsDirentArticle tab={activeTab} /></div>));
    case 'folder': return (activeTab && (<div className={className}><FsDirentFolder tab={activeTab} /></div>));
    case 'link': return (activeTab && (<div className={className}><FsDirentLink tab={activeTab} /></div>));
    case 'phone': return (activeTab && (<div className={className}><FsDirentPhone tab={activeTab} /></div>));
    case 'service': return (activeTab && (<div className={className}><FsDirentService tab={activeTab} /></div>));
    case 'flow': return (activeTab && (<div className={className}><FsDirentFlow tab={activeTab} /></div>));
    case 'printout': return (activeTab && (<div className={className}><FsDirentPrintout tab={activeTab} /></div>));
    case 'template': return (activeTab && (<div className={className}><FsDirentTemplate tab={activeTab} /></div>));
    case 'dialob': return (activeTab && (<div className={className}><FsDirentDialob tab={activeTab} /></div>));
    case 'language': return (activeTab && (<div className={className}><FsDirentLanguage tab={activeTab} /></div>));
    case 'page': return (activeTab && (<div className={className}><FsDirentPage tab={activeTab} /></div>));
    default: return (<div className={className}>{intl.formatMessage({ id: 'fs.main.message.noAssetSelected' })}</div>);
  }
};

