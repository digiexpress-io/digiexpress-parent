import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentDecisionTableRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';
import { FsDirentDecisionTableCreateProps } from './FsDirentDecisionTableProps';


export const FsDirentDecisionTableCreate: React.FC<FsDirentDecisionTableCreateProps> = (_props) => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();

  return (
    <FsDirentDecisionTableRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.decision_table.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.nameField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.decision_table.nameField.placeholder' })} required />

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonSave />
        </div>

      </div>
    </FsDirentDecisionTableRoot>
  );
};
