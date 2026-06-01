import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentFlowTaskRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';
import { FsDirentFlowTaskCreateProps } from './FsDirentFlowTaskProps';


export const FsDirentFlowTaskCreate: React.FC<FsDirentFlowTaskCreateProps> = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();

    return (
      <FsDirentFlowTaskRoot className={classes.root} ownerState={ownerState}>
        <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.flow_task.sectionTitle.createNew' })}</Typography>
        <div className={classes.formContainer}>

          <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.flow_task.taskNameField.label' })}</Typography>
          <FsDirentTextField
            required
            value={ownerState.name}
            placeholder={intl.formatMessage({ id: 'fs.dirent.flow_task.taskNameField.placeholder' })}
            onChange={ownerState.onChangeName}
            onBlur={ownerState.onBlurName}
          />

          <div className={classes.buttonContainer}>
            <FsDirentButtonCancel onClick={ownerState.onCancel} />
            <FsDirentButtonSave onClick={ownerState.onSave} />
          </div>

        </div>
      </FsDirentFlowTaskRoot>
    );
  };