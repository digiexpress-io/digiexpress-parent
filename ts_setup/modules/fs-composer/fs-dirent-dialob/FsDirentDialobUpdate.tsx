import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentDialobRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentDialobUpdateProps } from './FsDirentDialobProps';

export const FsDirentDialobUpdate: React.FC<FsDirentDialobUpdateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentDialobRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.dialob.sectionTitle.edit' }, { name: ownerState.assetPath })}</Typography>
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

      </div>
    </FsDirentDialobRoot>
  );
};
