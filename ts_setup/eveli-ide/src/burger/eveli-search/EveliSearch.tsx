import React from 'react';
import { TextField, Typography } from '@mui/material';
import { Stack, useThemeProps } from '@mui/system';
import { MUI_NAME, useUtilityClasses, EveliSearchRoot } from './useUtilityClasses';



export interface EveliSearchProps {
  children: React.ReactNode;
}


export const EveliSearch: React.FC<EveliSearchProps> = (initProps) => {

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const ownerState = {
    ...props
  }
  const classes = useUtilityClasses();


  return (
    <EveliSearchRoot className={classes.root} ownerState={ownerState}>
      <div className={classes.searchFieldContainer}>
        <Typography className={classes.searchFieldContainerTitle}>Search</Typography>
        <TextField type='search' className={classes.searchField} />
      </div>

      <Stack direction="row" spacing={1} justifyContent='center' mt={1}>
        {props.children}
      </Stack>
    </EveliSearchRoot>
  )
}