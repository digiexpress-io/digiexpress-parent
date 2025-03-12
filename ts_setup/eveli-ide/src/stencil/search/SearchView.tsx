import React from 'react';
import { Chip } from '@mui/material';

import { EveliSearch } from '../../burger'

export type AssetType = 'ARTICLES' | 'SERVICES' | 'LINKS' | 'MIGRATIONS' | 'LOCALES' | 'PAGES' | 'TEMPLATES' | 'ALL';

export const SearchView: React.FC = () => {
  
  return (
      <EveliSearch>
        <Chip label="All" />
        <Chip label="Articles" />
        <Chip label="Pages" />
        <Chip label="Services" />
        <Chip label="Links" />
        <Chip label="Locales" />
        <Chip label="Migrations" />
        <Chip label="Templates" />
      </EveliSearch>
   )
}


