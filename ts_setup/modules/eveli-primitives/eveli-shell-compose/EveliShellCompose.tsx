import * as React from 'react';
import { Divider, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { EveliShellComposeRoot, useUtilityClasses } from './useUtilityClasses';



export interface EveliShellComposeProps {
  anchorEl: HTMLElement | null,
  onClose: () => void,
  open: boolean,
  children: React.ReactNode
}


export const EveliShellCompose: React.FC<EveliShellComposeProps> = (props) => {
  const classes = useUtilityClasses();

  return (
    <EveliShellComposeRoot className={classes.root}
      open={props.open}
      anchorEl={props.anchorEl}
      onClose={props.onClose}
      anchorOrigin={{
        vertical: 'top',
        horizontal: 'right',
      }}
    >
      <Typography className={classes.title}><FormattedMessage id='new' defaultMessage='New'/></Typography>
      <Divider />
      {props.children}
    </EveliShellComposeRoot>
  );
}
