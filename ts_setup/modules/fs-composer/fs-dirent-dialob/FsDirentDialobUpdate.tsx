import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsDirent } from '@dxs-ts/fs-api';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentTextFieldAutocomplete } from '../fs-dirent-textfield-autocomplete';
import { useUtilityClasses, FsDirentDialobRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentDialobUpdateProps } from './FsDirentDialobProps';

export const FsDirentDialobUpdate: React.FC<FsDirentDialobUpdateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();
  const { selectOptions } = useFsDirent();
  const [labels, setLabels] = React.useState<string[]>((ownerState.dirent?.labels ?? []).map(l => l.value));

  return (
    <FsDirentDialobRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.dialob.sectionTitle.edit' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.dialob.technicalNameField.label' })}</Typography>
        <FsDirentTextField
          value={ownerState.technicalName}
          placeholder={intl.formatMessage({ id: 'fs.dirent.dialob.technicalNameField.placeholder' })}
          onChange={ownerState.onChangeTechnicalName}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.dialob.formNameField.label' })}</Typography>
        <FsDirentTextField
          value={ownerState.formName}
          placeholder={intl.formatMessage({ id: 'fs.dirent.dialob.formNameField.placeholder' })}
          onChange={ownerState.onChangeFormName}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.dialob.labelsField.label' })}</Typography>
        <FsDirentTextFieldAutocomplete
          options={selectOptions.labels}
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
