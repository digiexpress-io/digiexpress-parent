import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentFormField, FsDirentTextField } from '../fs-utilities';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { useUtilityClasses, FsDirentFlowRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';


export const FsDirentFlowCreate: React.FC = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();

    return (
      <FsDirentFlowRoot className={classes.root} ownerState={ownerState}>
        <div className={classes.titleRow}>
          <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.flow.sectionTitle.createNew' })}</Typography>
          <FsDirentButtonSave onClick={ownerState.onSave} disabled={!ownerState.isDirty} />
        </div>
        <div className={classes.formContainer}>

          <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.nameField.label' })}>
            <FsDirentTextField required
              value={ownerState.name}
              placeholder={intl.formatMessage({ id: 'fs.dirent.flow.nameField.placeholder' })}
              onChange={ownerState.onChangeName}
            />
          </FsDirentFormField>

        </div>
      </FsDirentFlowRoot>
    );
  };