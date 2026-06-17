import React from 'react';
import { useIntl } from 'react-intl';
import { FsDirentFormField, FsDirentTextField } from '../fs-utilities';

import { useUtilityClasses, FsDirentFlowTaskRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';


export const FsDirentFlowTaskCreate: React.FC = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();


  return (
    <FsDirentFlowTaskRoot className={classes.root} ownerState={ownerState}>
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
