import React from 'react';
import { Chip } from '@mui/material';

import { useIntl } from 'react-intl';


import { OwnerState, useUtilityClasses, GRouterSecuredServicesFilterButtonsRoot } from './useUtilityClasses';
import { SearchApi } from '../api-search';
import { GSecuredServicesSearch } from '../g-secured-services-search';



export const SearchFilters: React.FC<{ ownerState: OwnerState }> = ({ ownerState }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const { search, setSearch } = ownerState;

  function handleFilterByType(type: SearchApi.FilterMode) {
    setSearch(prev => prev.filterMode(prev.searchOptionType === type ? 'ALL' : type));
  }
  return (<>
    <GSecuredServicesSearch id='gamut.search.placeholder' onChange={({ currentTarget }) => setSearch(prev => prev.find(currentTarget.value))} />
    <GRouterSecuredServicesFilterButtonsRoot className={classes.searchFilterButtons}>
      <Chip
        color={search.searchOptionType === 'ALL' ? 'primary' : undefined}
        label={intl.formatMessage({ id: 'gamut.search.results.allResults' })}
        onClick={() => handleFilterByType('ALL')} />
      <Chip
        color={search.searchOptionType === 'TOPICS' ? 'primary' : undefined}
        label={intl.formatMessage({ id: 'gamut.search.popover.allServices' })}
        onClick={() => handleFilterByType('TOPICS')} />
      <Chip
        color={search.searchOptionType === 'FORM_LINKS' ? 'primary' : undefined}
        label={intl.formatMessage({ id: 'gamut.search.popover.allForms' })}
        onClick={() => handleFilterByType('FORM_LINKS')} />
      <Chip
        color={search.searchOptionType === 'PHONE_LINKS' ? 'primary' : undefined}
        label={intl.formatMessage({ id: 'gamut.search.popover.allPhones' })}
        onClick={() => handleFilterByType('PHONE_LINKS')} />
      <Chip
        color={search.searchOptionType === 'LINKS' ? 'primary' : undefined}
        label={intl.formatMessage({ id: 'gamut.search.popover.allLinks' })}
        onClick={() => handleFilterByType('LINKS')} />
    </GRouterSecuredServicesFilterButtonsRoot>
  </>
  );
}