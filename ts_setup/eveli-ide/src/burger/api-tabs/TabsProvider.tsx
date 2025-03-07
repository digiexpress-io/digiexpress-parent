import React from 'react';
import { ManyTabsImpl } from './tab-types-impl';
import { ManyTabs, OneTab, TabsContextType } from './tab-api';


const TabsContext = React.createContext<TabsContextType>({} as any)


export const TabsProvider: React.FC<{ children: React.ReactNode, onTabClose?: (tab: OneTab<any>, nextActive: OneTab<any> | undefined) => void}> = ({children, onTabClose}) => {

  const [state, setState] = React.useState<ManyTabs>(new ManyTabsImpl({onTabClose}));
  
  const setters = React.useMemo(() => {
    function handleTabAdd(newItem: OneTab<any>) {
      setState(prev => prev.withTab(newItem));
    }

    function handleTabAddAll(newItems: OneTab<any>[]) {
      setState(prev => newItems.reduce((collector, newItem) => collector.withTab(newItem), prev));
    }

    function handleTabChange(tabIndex: number) {
      setState(prev => prev.withTab(tabIndex));
    }
    
    function handleTabClose(tab: OneTab<any>) {
      setState(prev => {
        const active = prev.history.open;
        const tab = prev.tabs[active];
        const result = prev.deleteTab(tab.id);
        return result;
      });
    }

    function handleTabCloseAll() {
      setState(prev => prev.deleteTabs());
    }

    function handleTabData(tabId: string, updateCommand: (oldData: any) => any) {
      setState(prev => prev.withTabData(tabId, updateCommand));
    }

    function handleTabCloseCurrent() {
      setState(prev => {
        const active = prev.history.open;
        const tab = prev.tabs[active];
        const result = prev.deleteTab(tab.id);
        return result;
      });
    }
    return { handleTabAdd, handleTabChange, handleTabClose, handleTabCloseAll, handleTabData, handleTabCloseCurrent, handleTabAddAll }
  }, [setState])

  const context: TabsContextType = React.useMemo(() => ({ session: state, ...setters }), [state, setters])
  return (<TabsContext.Provider value={context}>{children}</TabsContext.Provider>);
}

export const useTabs = () => {
  const result: TabsContextType = React.useContext(TabsContext);
  return result;
}