import React, { createContext, useContext, useMemo, useState } from 'react';
import { useFsNav, useFsDirent } from '@dxs-ts/fs-api';
import type { FilterData } from './search-helpers';

interface FsSearchContextType {
  searchTerm: string;
  activeFilters: FilterData[];
  open: boolean;
  isDarkMode: boolean;
  allAvailableFilters: FilterData[];
  
  setSearchTerm: (value: string) => void;
  setActiveFilters: (filters: FilterData[]) => void;
  setOpen: (isOpen: boolean) => void;
  handleSearchChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
  handleFilterSelectChange: (selectedLabels: string[]) => void;
}


const FsSearchContext = createContext<FsSearchContextType | undefined>(undefined);

export const FsSearchProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isDarkMode } = useFsNav();
  const { selectOptions } = useFsDirent();

  const [searchTerm, setSearchTerm] = useState('');
  const [activeFilters, setActiveFilters] = useState<FilterData[]>([]);
  const [open, setOpen] = useState(false);

  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(event.target.value);
  };

  const handleFilterSelectChange = (selectedLabels: string[]) => {
    const selectedFilters = allAvailableFilters.filter(filter =>
      selectedLabels.includes(filter.label)
    );
    setActiveFilters(selectedFilters);
  };

  const contextValue = useMemo(() => ({
    searchTerm,
    activeFilters,
    open,
    isDarkMode,
    allAvailableFilters,
    availableLabelOptions: selectOptions.labels,
    setSearchTerm,
    setActiveFilters,
    setOpen,
    handleSearchChange,
    handleFilterSelectChange
  }), [searchTerm, activeFilters, open, isDarkMode, selectOptions.labels]);

  return (
    <FsSearchContext.Provider value={contextValue}>
      {children}
    </FsSearchContext.Provider>
  );
};

export const useFsSearch = () => {
  const context = useContext(FsSearchContext);
  if (!context) {
    throw new Error('useFsSearch must be used within a FsSearchProvider');
  }
  return { search: context };
};


export const allAvailableFilters: FilterData[] = [
  { label: 'Articles', type: 'article' },
  { label: 'Dialobs', type: 'dialob' },
  { label: 'Services', type: 'service' },
  { label: 'Pages', type: 'folder' },
  { label: 'Links', type: 'link' },
  { label: 'Phone Numbers', type: 'phone' },
  { label: 'Languages', type: 'language' },
  { label: 'Flows', type: 'flow' },
  { label: 'Printouts', type: 'printout' },
  { label: 'Images', type: 'image' }
];