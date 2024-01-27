import React from 'react';

import { Tab, TabId, TabBody, SelectionOptions, TabSelection, TabbingInit } from './tabbing-types';
import { ImmutableTab } from './ImmutableTab';
import { 
  tabBodyReducer, tabActivityReducer, tabSelectionReducer, 
  WithTabBody, WithTabActivity, WithTabSelecion, initTabs
} from './initMethods';


export interface TabbingContextType<I extends TabId, B extends TabBody> {
  tabs: readonly Tab<I, B>[];
  getActiveTab(): Tab<I, B>;

  withTabBody: (tabId: I, setter: (oldBody: B) => B) => void;
  withTabActivity: (tabId: I, options?: SelectionOptions) => void;
  withTabSelecion: (tabId: I, item: TabSelection, options?: SelectionOptions) =>  void;
}

export interface TabbingProps<I extends TabId, B extends TabBody> {
  children: React.ReactElement;
  init: TabbingInit<I, B>;
}

export interface FactoryCreatedContext<I extends TabId, B extends TabBody> {
  Context: React.Context<TabbingContextType<I, B>>;
  Provider: React.FC<TabbingProps<I, B>>;
  hooks: {
    useTabbing: () => TabbingContextType<I, B>;
  };
}

export function getInstance<I extends TabId, B extends TabBody>(): FactoryCreatedContext<I, B> {


  const TabbingContext = React.createContext<TabbingContextType<I, B>>({} as any);

  function TabbingProvider(props: TabbingProps<I, B>) {
    const [tabs, setTabs] = React.useState<readonly ImmutableTab<I, B>[]>(initTabs(props.init));

    const withTabBody: WithTabBody<I, B> = React.useCallback((tabId, setter) => tabBodyReducer(tabId, setter, setTabs), [setTabs]);
    const withTabActivity: WithTabActivity<I> = React.useCallback((tabId, options) => tabActivityReducer(tabId, options, setTabs), [setTabs]);
    const withTabSelecion: WithTabSelecion<I> = React.useCallback((tabId, item, options) => tabSelectionReducer(tabId, item, options, setTabs), [setTabs]);

    const contextValue: TabbingContextType<I, B> = React.useMemo(() => {

      function getActiveTab() {
        const found = tabs.find(t => t.active);
        //console.assert(found, "No tabs are active");
        return found!;
      }
      return { tabs, withTabBody, withTabActivity, withTabSelecion, getActiveTab };
    },[tabs, withTabBody, withTabActivity, withTabSelecion]);

    return (<TabbingContext.Provider value={contextValue}>{props.children}</TabbingContext.Provider>);
  }

  function useTabbing() {
    const result: TabbingContextType<I, B> = React.useContext(TabbingContext);
    return result;
  }

  return {
    Context: TabbingContext,
    Provider: TabbingProvider,
    hooks: { useTabbing }
  };
}