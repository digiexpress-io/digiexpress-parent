import React, { createContext, useContext, useMemo, useState } from 'react';
import { useFsDirent } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { allAvailableTypeFilters, AssetTypeFilter, FilterData, LabelFilter } from './search-helpers';

interface FsSearchContextType {
  searchTerm: string;
  activeFilters: FilterData[];
  open: boolean;
  isDarkMode: boolean;
  allAvailableTypeFilters: AssetTypeFilter[];
  availableLabelOptions: string[];

  setSearchTerm: (value: string) => void;
  setActiveFilters: (filters: FilterData[]) => void;
  setOpen: (isOpen: boolean) => void;
  handleFilterSelectChange: (selectedLabels: string[]) => void;
  handleLabelFilterSelectChange: (selectedValues: string[]) => void;
}


const FsSearchContext = createContext<FsSearchContextType | undefined>(undefined);

export const FsSearchProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isDarkMode } = useFsTheme();
  const { selectOptions } = useFsDirent();

  const [searchTerm, setSearchTerm] = useState('');
  const [activeFilters, setActiveFilters] = useState<FilterData[]>([]);
  const [open, setOpen] = useState(false);

  const handleFilterSelectChange = (selectedValues: string[]) => {
    const newTypeFilters = allAvailableTypeFilters.filter(f => selectedValues.includes(f.value));
    const currentLabelFilters = activeFilters.filter((f): f is LabelFilter => f.type === 'label');
    setActiveFilters([...currentLabelFilters, ...newTypeFilters]);
  };

  const handleLabelFilterSelectChange = (selectedValues: string[]) => {
    const newLabelFilters: LabelFilter[] = selectedValues.map(v => ({ type: 'label', label: v, value: v }));
    const currentTypeFilters = activeFilters.filter((f): f is AssetTypeFilter => f.type === 'asset');
    setActiveFilters([...currentTypeFilters, ...newLabelFilters]);
  };

  const contextValue = useMemo(() => ({
    searchTerm,
    activeFilters,
    open,
    isDarkMode,
    allAvailableTypeFilters,
    availableLabelOptions: selectOptions.labels,
    setSearchTerm,
    setActiveFilters,
    setOpen,
    handleFilterSelectChange,
    handleLabelFilterSelectChange,
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


