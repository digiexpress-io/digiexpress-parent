import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentSelectSingle } from '../fs-dirent-select-single';
import { FsDirentTextFieldAutocomplete } from '../fs-dirent-textfield-autocomplete';
import { useUtilityClasses, FsDirentPrintoutRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';
import { FsDirentPrintoutCreateProps } from './FsDirentPrintoutProps';

export const FsDirentPrintoutCreate: React.FC<FsDirentPrintoutCreateProps> = (_props) => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();

  return (
    <FsDirentPrintoutRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.printout.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.nameField.label' })}</Typography>
        <FsDirentTextField required
          placeholder={intl.formatMessage({ id: 'fs.dirent.printout.nameField.placeholder' })}
          value={ownerState.serviceName}
          onChange={ownerState.onChangeServiceName}
          onBlur={ownerState.onBlurServiceName}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.printout.orchestratorNameField.label' })}</Typography>
        <FsDirentSelectSingle
          options={ownerState.flows}
          value={ownerState.orchestratorName}
          onChange={ownerState.onChangeOrchestratorName}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.descriptionField.label' })}</Typography>
        <FsDirentTextField
          multiline minRows={2} maxRows={5}
          placeholder={intl.formatMessage({ id: 'fs.dirent.descriptionField.placeholder' })}
          value={ownerState.assetDescription}
          onChange={ownerState.onChangeDescription}
          onBlur={ownerState.onBlurDescription}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.labelsField.label' })}</Typography>
        <FsDirentTextFieldAutocomplete
          options={ownerState.labelOptions}
          value={ownerState.labels}
          onChange={ownerState.onChangeLabels}
          placeholder={intl.formatMessage({ id: 'fs.dirent.labelsField.placeholder' })}
        />

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel onClick={ownerState.onCancel} />
          <FsDirentButtonSave onClick={ownerState.onSave} disabled={!ownerState.isChanged} />
        </div>

      </div>
    </FsDirentPrintoutRoot>
  );
};
