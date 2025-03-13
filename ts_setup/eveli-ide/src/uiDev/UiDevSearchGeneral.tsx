import React from 'react';
import { Chip, TextField, Typography, InputAdornment} from '@mui/material';
import { Stack, useThemeProps } from '@mui/system';
import { MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { UiDevAppSearchRoot } from './useUtilityClasses';
import { AssetType } from './types';

export interface UiDevSearchProps {

}


export const UiDevSearchGeneral: React.FC<UiDevSearchProps> = (initProps) => {

  const [activeFilter, setActiveFilter] = React.useState<AssetType>('ALL');

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const ownerState = {
    ...props
  }
  const classes = useUtilityClasses();

  function handleFilterClick(buttonId: AssetType) {
    setActiveFilter(buttonId);
  };

  function handleClassName(buttonId: AssetType): string {
    return activeFilter === buttonId ? classes.searchFilterActive : classes.searchFilter;
  }



  return (
    <UiDevAppSearchRoot className={classes.root} ownerState={ownerState}>
      <div className={classes.searchFieldContainer}>
        <Typography className={classes.searchFieldContainerTitle}>Search</Typography>
        <TextField type='search' className={classes.searchField} />
      </div>

      <Stack direction="row" spacing={1} justifyContent='center' mt={1}>
        <Chip label="All" onClick={() => handleFilterClick('ALL')} className={handleClassName('ALL')} />
        <Chip label="Articles" onClick={() => handleFilterClick('ARTICLES')} className={handleClassName('ARTICLES')}  />
        <Chip label="Pages" onClick={() => handleFilterClick('PAGES')} className={handleClassName('PAGES')}/>
        <Chip label="Services" onClick={() => handleFilterClick('SERVICES')} className={handleClassName('SERVICES')} />
        <Chip label="Links" onClick={() => handleFilterClick('LINKS')} className={handleClassName('LINKS')} />
        <Chip label="Locales" onClick={() => handleFilterClick('LOCALES')} className={handleClassName('LOCALES')} />
        <Chip label="Migrations" onClick={() => handleFilterClick('MIGRATIONS')} className={handleClassName('MIGRATIONS')} />
        <Chip label="Templates" onClick={() => handleFilterClick('TEMPLATES')} className={handleClassName('TEMPLATES')}/>
      </Stack>
    </UiDevAppSearchRoot>
  )
}