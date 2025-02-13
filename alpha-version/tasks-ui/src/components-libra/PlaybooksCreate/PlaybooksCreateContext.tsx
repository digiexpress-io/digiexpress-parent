import React from 'react';
import { getInstance as createTabsContext, SingleTabInit, Tab } from 'descriptor-tabbing';


// New role related
export interface NewPlaybook {
  name: string;
  description: string;
}

export interface NewPlaybookContextType {
  entity: NewPlaybook;
  setName(newName: string): void;
  setDescription(newDescription: string): void;
}

const NewPlaybookContext = React.createContext<NewPlaybookContextType>({} as any);
const NewPlaybookProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [entity, setNewPlaybook] = React.useState<NewPlaybook>({
    name: "",
    description: ""
  });

  const setName = React.useCallback((name: string) => setNewPlaybook(previous => Object.freeze({ ...previous, name })), []);
  const setDescription = React.useCallback((description: string) => setNewPlaybook(previous => Object.freeze({ ...previous, description })), []);


  const contextValue: NewPlaybookContextType = React.useMemo(() => {
    return {
      entity,
      setName,
      setDescription,
    }
  }, [entity, setName, setDescription ]);

  return (<NewPlaybookContext.Provider value={contextValue}>{children}</NewPlaybookContext.Provider>);
}

export function useNewPlaybook(): NewPlaybookContextType {
  const result: NewPlaybookContextType = React.useContext(NewPlaybookContext);
  return result;
}


// Tabs related
const TabsContext = createTabsContext<TabTypes, TabState>();
function initAllTabs(): Record<TabTypes, SingleTabInit<TabState>> {
  return {
    role_parent: { body: {}, active: true },
    role_permissions: { body: {}, active: false },
    role_members: { body: {}, active: false }
  };
}

export type TabTypes = 'role_parent' | 'role_permissions' | 'role_members';
export interface TabState { }
export function useTabs() {
  const tabbing = TabsContext.hooks.useTabbing();
  const activeTab: Tab<TabTypes, TabState> = tabbing.getActiveTab();
  function setActiveTab(next: TabTypes) {
    tabbing.withTabActivity(next, { disableOthers: true });
  }
  return { activeTab, setActiveTab };
}

// Root of all
export const PlaybooksCreateProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <TabsContext.Provider init={initAllTabs()}>
      <NewPlaybookProvider>
        <>{children}</>
      </NewPlaybookProvider>
    </TabsContext.Provider>
  );
}