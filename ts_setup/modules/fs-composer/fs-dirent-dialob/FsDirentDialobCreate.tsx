import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentTextFieldAutocomplete } from '../fs-dirent-textfield-autocomplete';
import { useUtilityClasses, FsDirentDialobRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';
import { FsDirentDialobCreateProps } from './FsDirentDialobProps';

const LABEL_OPTIONS = ['label-a', 'label-b', 'label-c'];

export const FsDirentDialobCreate: React.FC<FsDirentDialobCreateProps> = (_props) => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();
  const [labels, setLabels] = React.useState<string[]>([]);

  return (
    <FsDirentDialobRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.dialob.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.dialob.technicalNameField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.dialob.technicalNameField.placeholder' })} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.dialob.formNameField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.dialob.formNameField.placeholder' })} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.dialob.labelsField.label' })}</Typography>
        <FsDirentTextFieldAutocomplete
          options={LABEL_OPTIONS}
          value={labels}
          onChange={setLabels}
          placeholder={intl.formatMessage({ id: 'fs.dirent.dialob.labelsField.placeholder' })}
        />

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentDialobRoot>
  );
};
