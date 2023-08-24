import React from "react";
import { MenuContextType, MenuTab } from "./menu-ctx-types";

const MenuContext = React.createContext<MenuContextType>({} as MenuContextType);

const MenuProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {

  const [activeTab, setActiveTab] = React.useState<MenuTab>('messages');

  const setter = React.useCallback((tab: MenuTab) => setActiveTab(tab), [setActiveTab]);

  const resetter = React.useCallback(() => setActiveTab('messages'), [setActiveTab]);

  const contextValue: MenuContextType = React.useMemo(() => {
    return { activeTab, withTab: setter, resetTab: resetter };
  }, [activeTab, setter, resetter]);

  return (<MenuContext.Provider value={contextValue}>{children}</MenuContext.Provider>);
};

export { MenuProvider, MenuContext };
