import React from 'react';
import { TextField, Typography } from '@mui/material';
import { Stack, useThemeProps } from '@mui/system';
import { MUI_NAME, useUtilityClasses, EveliSearchRoot } from './useUtilityClasses';

import { useIntl } from 'react-intl';


export interface EveliSearchProps {
  children: React.ReactNode;
}


export const EveliSearch: React.FC<EveliSearchProps> = (initProps) => {
  const intl = useIntl();
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
        <Typography className={classes.searchFieldContainerTitle}>{intl.formatMessage({ id: 'search.field.label' })}</Typography>
        <TextField type='search' className={classes.searchField} />
      </div>

      <Stack direction="row" spacing={1} justifyContent='center' mt={1}>
        {props.children}
      </Stack>

      <Stack direction='column'>
        <div>Search results1</div>
        <div>Search results2</div>
        <div>Search results3</div>
        <div>Search results4</div>
        <div>Search results5</div>
      </Stack>
    </EveliSearchRoot>
  )
}