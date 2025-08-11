import React from 'react';
import { Alert, AlertTitle } from '@mui/material';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import { EveliAlertRoot, useUtilityClasses } from './useUtilityClasses';


export const EveliAlert: React.FC<{ title: string, body?: string }> = ({ title, body }) => {
  const classes = useUtilityClasses();

  return (
    <EveliAlertRoot className={classes.root}>
      <Alert icon={<InfoOutlinedIcon />}>
        <AlertTitle>{title}</AlertTitle>
        {body}
      </Alert>
    </EveliAlertRoot>
  )
}