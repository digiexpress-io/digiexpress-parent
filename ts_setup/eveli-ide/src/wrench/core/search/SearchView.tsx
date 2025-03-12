import React from 'react';
import { Chip } from '@mui/material';
import { useIntl } from 'react-intl';
import { EveliSearch } from '../../../burger'


export const SearchView: React.FC = () => {
  const intl = useIntl();

  return (
    <EveliSearch>
      <Chip label={intl.formatMessage({ id: 'search.all' })} />
      <Chip label={intl.formatMessage({ id: 'search.flows' })} />
      <Chip label={intl.formatMessage({ id: 'search.decisions' })} />
      <Chip label={intl.formatMessage({ id: 'search.services' })} />
    </EveliSearch>
  )
}


