import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentFlowRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';
import { FsDirentFlowCreateProps } from './FsDirentFlowProps';


export const FsDirentFlowCreate: React.FC<FsDirentFlowCreateProps> = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();

    return (
      <FsDirentFlowRoot className={classes.root} ownerState={ownerState}>
        <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.flow.sectionTitle.createNew' })}</Typography>
        <div className={classes.formContainer}>

          <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.nameField.label' })}</Typography>
          <FsDirentTextField required
            value={ownerState.name}
            placeholder={intl.formatMessage({ id: 'fs.dirent.flow.nameField.placeholder' })}
            onChange={ownerState.onChangeName}
          />

        </div>
      </FsDirentFlowRoot>
    );
  };