import React from 'react';
import { useIntl } from 'react-intl';
import { Typography } from '@mui/material';
import { FsDirentFormField, FsDirentTextField } from '../fs-utilities';
import { FsDirentButtonSave } from '../fs-dirent-button-save';

import { useUtilityClasses, FsDirentFlowTaskRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';


export const FsDirentFlowTaskCreate: React.FC = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();


  return (
    <FsDirentFlowTaskRoot className={classes.root}>
      <div className={classes.titleRow}>
        <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.flow_task.sectionTitle.createNew' })}</Typography>
        <FsDirentButtonSave onClick={ownerState.onSave} disabled={!ownerState.isDirty} />
      </div>
      <div className={classes.formContainer}>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.flow_task.taskNameField.label' })}>
          <FsDirentTextField required
            value={ownerState.name}
            placeholder={intl.formatMessage({ id: 'fs.dirent.flow_task.taskNameField.placeholder' })}
            onChange={ownerState.onChangeName}
          />
        </FsDirentFormField>

      </div>
    </FsDirentFlowTaskRoot>
  );
};
