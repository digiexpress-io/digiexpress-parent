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



  if (!dirent) {
    return (
      <div key={tabId} className={className}>
        <FsMainDefaultBackground />
      </div>);
  }

  const widget = createWidget(dirent);
  const DirentView = activeTab.type === 'edit' ? widget.views.UpdateView : widget.views.CreateView;

  return (
    <div key={tabId} className={className}>
      <DirentView direntId={dirent.id} />
    </div>
  )
/*
  switch (tabType) {
    case 'ARTICLE': {
      return (activeTab.type === 'edit' ? <widget.views.UpdateView direntId={dirent.id} /> : <widget.views.CreateView />)
    }

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
        */
};

