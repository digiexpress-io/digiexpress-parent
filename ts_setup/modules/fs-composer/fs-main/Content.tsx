import React from 'react';
import { useIntl } from 'react-intl';
import { FsTab } from '@dxs-ts/fs-nav';
import { OwnerState } from './useOwnerState';
import { FsDirentArticle } from '../fs-dirent-article';
import { FsDirentFolder } from '../fs-dirent-folder';
import { FsDirentLink } from '../fs-dirent-link';
import { FsDirentPhone } from '../fs-dirent-phone';
import { FsDirentWorkflow } from '../fs-dirent-workflow';
import { FsDirentLocale } from '../fs-dirent-locale';
import { FsDirentPage } from '../fs-dirent-page';
import { FsDirentFlow } from '../fs-dirent-flow';
import { FsDirentDialob } from '../fs-dirent-dialob';
import { FsDirentPrintout } from '../fs-dirent-printout';
import { FsDirentTemplate } from '../fs-dirent-template';
import { FsDirentFlowTask } from '../fs-dirent-flow-task';
import { FsDirentDecisionTable } from '../fs-dirent-decision-table';


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
  const intl = useIntl();
  const activeTab = ownerState.openTabs[ownerState.activeTabIndex];
  const { tabType, tabId } = resolveTab(activeTab);

  switch (tabType) {
    case 'ARTICLE': return (activeTab && (<div key={tabId} className={className}><FsDirentArticle tab={activeTab} /></div>));
    case 'FOLDER': return (activeTab && (<div key={tabId} className={className}><FsDirentFolder tab={activeTab} /></div>));
    case 'ARTICLE_LINK': return (activeTab && (<div key={tabId} className={className}><FsDirentLink tab={activeTab} /></div>));
    case 'ARTICLE_WORKFLOW': return (activeTab && (<div key={tabId} className={className}><FsDirentWorkflow tab={activeTab} /></div>));
    case 'FLOW': return (activeTab && (<div key={tabId} className={className}><FsDirentFlow tab={activeTab} /></div>));
    case 'FLOW_TASK': return (activeTab && (<div key={tabId} className={className}><FsDirentFlowTask tab={activeTab} /></div>));
    case 'DECISION_TABLE': return (activeTab && (<div key={tabId} className={className}><FsDirentDecisionTable tab={activeTab} /></div>));
    case 'PRINTOUT': return (activeTab && (<div key={tabId} className={className}><FsDirentPrintout tab={activeTab} /></div>));
    case 'ARTICLE_TEMPLATE': return (activeTab && (<div key={tabId} className={className}><FsDirentTemplate tab={activeTab} /></div>));
    case 'DIALOB_FORM': return (activeTab && (<div key={tabId} className={className}><FsDirentDialob tab={activeTab} /></div>));
    case 'DIALOB_FORM_META': return (activeTab && (<div key={tabId} className={className}><FsDirentDialob tab={activeTab} /></div>));
    case 'LOCALE': return (activeTab && (<div key={tabId} className={className}><FsDirentLocale tab={activeTab} /></div>));
    case 'ARTICLE_PAGE': return (activeTab && (<div key={tabId} className={className}><FsDirentPage tab={activeTab} /></div>));
    default: return (<div className={className}>{intl.formatMessage({ id: 'fs.main.message.noAssetSelected' })}</div>);
  }
};

