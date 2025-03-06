import React from 'react';
import { TextField, InputAdornment } from '@mui/material';
import { useThemeProps } from '@mui/system';
import { MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { UiDevAppSearchRoot } from './useUtilityClasses';
import SearchIcon from '@mui/icons-material/Search';


export interface UiDevSearchProps {

}


export const UiDevSearchAsset: React.FC<UiDevSearchProps> = (initProps) => {

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const ownerState = {
    ...props
  }
  const classes = useUtilityClasses();


  return (
    <UiDevAppSearchRoot className={classes.root} ownerState={ownerState}>
      <div className={classes.searchFieldContainer}>
        <TextField type='search' className={classes.searchField}
          placeholder='Search Articles'
          slotProps={{
            input: {
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              )
            },
          }}
        />
      </div>

    </UiDevAppSearchRoot>
  )
}