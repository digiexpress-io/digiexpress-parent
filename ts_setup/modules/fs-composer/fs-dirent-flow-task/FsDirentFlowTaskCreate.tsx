import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentFlowTaskRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';


export const FsDirentFlowTaskCreate: React.FC = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();


  return (
    <FsDirentFlowTaskRoot className={classes.root} ownerState={ownerState}>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.flow_task.taskNameField.label' })}</Typography>
        <FsDirentTextField required
          value={ownerState.name}
          placeholder={intl.formatMessage({ id: 'fs.dirent.flow_task.taskNameField.placeholder' })}
          onChange={ownerState.onChangeName}
        />

      </div>
    </FsDirentFlowTaskRoot>
  );
};
