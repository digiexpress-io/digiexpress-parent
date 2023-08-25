import React from "react";
import { MenuContextType, MenuTab } from "./menu-ctx-types";

const MenuContext = React.createContext<MenuContextType>({} as MenuContextType);

const useMenu = () => {
  const result: MenuContextType = React.useContext(MenuContext);
  return result;
}

const MenuProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {

  const [activeTab, setActiveTab] = React.useState<MenuTab>('messages');

  const setter = React.useCallback((tab: MenuTab) => setActiveTab(tab), [setActiveTab]);

  const contextValue: MenuContextType = React.useMemo(() => {
    return { activeTab, withTab: setter };
  }, [activeTab, setter]);

  return (<MenuContext.Provider value={contextValue}>{children}</MenuContext.Provider>);
};

export { MenuProvider, useMenu };
