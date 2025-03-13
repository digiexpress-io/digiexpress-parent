import React from 'react';
import { Chip } from '@mui/material';
import { useIntl } from 'react-intl';
import { EveliSearch } from '../../burger'


export const SearchView: React.FC = () => {
  const intl = useIntl();

  return (
    <EveliSearch>
      <Chip label={intl.formatMessage({ id: 'search.all' })} />
      <Chip label={intl.formatMessage({ id: 'search.articles' })} />
      <Chip label={intl.formatMessage({ id: 'search.services' })} />
      <Chip label={intl.formatMessage({ id: 'search.links' })} />
      <Chip label={intl.formatMessage({ id: 'search.templates' })} />
    </EveliSearch>
  )
}


