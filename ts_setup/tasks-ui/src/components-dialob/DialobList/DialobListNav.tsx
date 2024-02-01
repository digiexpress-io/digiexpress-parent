import React from 'react';

import { NavigationButton, FilterByString } from 'components-generic';
import { cyan } from 'components-colors';


import { NavigationSticky } from '../NavigationSticky';
import { FilterByTenant } from './FilterByTenant';
import DialobCreateDialog from '../DialobCreate';


export const DialobListNav: React.FC<{ onSearch: (value: string) => void }> = ({ onSearch }) => {
  const [createOpen, setCreateOpen] = React.useState(false);

  function handleCreateDialob() {
    setCreateOpen(prev => !prev);
  }

  function handleSearch(value: React.ChangeEvent<HTMLInputElement>) {
    onSearch(value.target.value);
  }

  return (<>
    <DialobCreateDialog open={createOpen} onClose={handleCreateDialob} setActiveDialob={handleCreateDialob} />
    <NavigationSticky>
      <FilterByString onChange={handleSearch} />
      <FilterByTenant />
      <NavigationButton id='dialob.form.create' values={undefined} active={false} color={cyan} onClick={handleCreateDialob} />
    </NavigationSticky>
  </>);
}

