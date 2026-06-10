import React from 'react';
import { FsMainDefaultBackground } from './FsMainDefaultBackground';
import { FsTab } from '@dxs-ts/fs-nav';
import { OwnerState } from './useOwnerState';
import { FsDirentArticle } from '../fs-dirent-article';
import { FsDirentFolder } from '../fs-dirent-folder';
import { FsDirentArticleLink } from '../fs-dirent-article-link';
import { FsDirentArticleWorkflow } from '../fs-dirent-article-workflow';
import { FsDirentLocale } from '../fs-dirent-locale';
import { FsDirentArticlePage } from '../fs-dirent-article-page';
import { FsDirentFlow } from '../fs-dirent-flow';
import { FsDirentDialob } from '../fs-dirent-dialob';
import { FsDirentPrintout } from '../fs-dirent-printout';
import { FsDirentPrintoutResource } from '../fs-dirent-printout-resource';
import { FsDirentPrintoutPage } from '../fs-dirent-printout-page';
import { FsDirentArticleTemplate } from '../fs-dirent-article-template';
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
  const activeTab = ownerState.openTabs[ownerState.activeTabIndex];
  const { tabType, tabId } = resolveTab(activeTab);


  if (!activeTab) {
    return (<FsMainDefaultBackground />);
  }

  switch (tabType) {
    case 'ARTICLE': return ((<div key={tabId} className={className}><FsDirentArticle tab={activeTab} /></div>));
    case 'FOLDER': return ((<div key={tabId} className={className}><FsDirentFolder tab={activeTab} /></div>));
    case 'ARTICLE_LINK': return ((<div key={tabId} className={className}><FsDirentArticleLink tab={activeTab} /></div>));
    case 'ARTICLE_WORKFLOW': return ((<div key={tabId} className={className}><FsDirentArticleWorkflow tab={activeTab} /></div>));
    case 'FLOW': return ((<div key={tabId} className={className}><FsDirentFlow tab={activeTab} /></div>));
    case 'FLOW_TASK': return ((<div key={tabId} className={className}><FsDirentFlowTask tab={activeTab} /></div>));
    case 'DECISION_TABLE': return ((<div key={tabId} className={className}><FsDirentDecisionTable tab={activeTab} /></div>));
    case 'PRINTOUT': return ((<div key={tabId} className={className}><FsDirentPrintout tab={activeTab} /></div>));
    case 'PRINTOUT_RESOURCE': return ((<div key={tabId} className={className}><FsDirentPrintoutResource tab={activeTab} /></div>));
    case 'PRINTOUT_PAGE': return ((<div key={tabId} className={className}><FsDirentPrintoutPage tab={activeTab} /></div>));
    case 'ARTICLE_TEMPLATE': return ((<div key={tabId} className={className}><FsDirentArticleTemplate tab={activeTab} /></div>));
    case 'DIALOB_FORM': return ((<div key={tabId} className={className}><FsDirentDialob tab={activeTab} /></div>));
    case 'DIALOB_FORM_META': return ((<div key={tabId} className={className}><FsDirentDialob tab={activeTab} /></div>));
    case 'LOCALE': return ((<div key={tabId} className={className}><FsDirentLocale tab={activeTab} /></div>));
    case 'ARTICLE_PAGE': return ((<div key={tabId} className={className}><FsDirentArticlePage tab={activeTab} /></div>));
    default: return (<FsMainDefaultBackground />);
  }
};

