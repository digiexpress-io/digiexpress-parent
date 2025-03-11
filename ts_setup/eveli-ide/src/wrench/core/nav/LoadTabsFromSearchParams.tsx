import { useSearch } from '@tanstack/react-router';
import React from 'react'
import { useTabs } from '@/burger';
import { toTab } from './wrench-nav-types';


export const LoadTabsFromSearchParams: React.FC = () => {
  const tabs = useTabs();
  const { explorer, explorerActive } = useSearch({ from: '/secured/$locale/assets/wrench/' });

  // load only once...
  React.useEffect(() => {
    tabs.handleTabAddAll(explorer.map(toTab));
  }, []);


  React.useEffect(() => {
    if(!explorerActive) {
      return;
    }
    if(tabs.session.activeTab?.id === explorerActive) {
      return;
    }

    const tab = tabs.session.findTab(explorerActive);
    if(!tab) {
      return;
    }


    tabs.handleTabChange(tab);

  }, [explorerActive]);

  return (<></>)
}

