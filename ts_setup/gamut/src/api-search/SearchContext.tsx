import React from 'react';
import { useIntl } from 'react-intl';

import { SearchApi } from './search-types';
import { useSite } from '../api-site';



export const SearchContext = React.createContext<SearchApi.SearchContextType>({} as any);

export const SearchProvider: React.FC<{ children: React.ReactNode }> = (props) => {
  const { views } = useSite();
  const intl = useIntl();
  const noValueIndicatorColon = intl.formatMessage({ id: 'gamut.noValueIndicatorColon' });
  const [state, setState] = React.useState(SearchApi.getInstance(views, noValueIndicatorColon));


  const find: (newSearchString: string) => void = React.useCallback((newSearchString) => (
    setState(prev => prev.find(newSearchString))
  ), []);
  const filterMode: (type: SearchApi.FilterMode) => void = React.useCallback((type) => (
    setState(prev => prev.filterMode(prev.searchOptionType === type ? 'ALL' : type))
  ), []);

  const contextValue: SearchApi.SearchContextType = React.useMemo(() => ({ value: state, find, filterMode }),
    [state, find, filterMode]);

  return (<SearchContext.Provider value={contextValue}>{props.children}</SearchContext.Provider>);
}

export function useSearch() {
  const result: SearchApi.SearchContextType = React.useContext(SearchContext);
  return result;
}