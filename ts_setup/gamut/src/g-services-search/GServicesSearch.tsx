import React from 'react';
import { TextField, useThemeProps } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { useIntl } from 'react-intl';
import { useUtilityClasses, GServicesSearchRoot, MUI_NAME } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';



export interface GServicesSearchProps {
  id: string;
  component?: GOverridableComponent<GServicesSearchProps>
}


export const GServicesSearch: React.FC<GServicesSearchProps> = (initProps) => {
  const intl = useIntl();

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  })

  const classes = useUtilityClasses(props);
  const ownerState = {
    ...props,
  }
  const Root = props.component ?? GServicesSearchRoot;
  return (
    <Root ownerState={ownerState} className={classes.root}>
      <TextField className={classes.input}
        slotProps={{ input: { startAdornment: <SearchIcon className={classes.icon} /> }}}
        placeholder={intl.formatMessage({ id: props.id })}></TextField>
    </Root >
  )
}


