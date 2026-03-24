import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentCreateDialobProps } from './FsDirentCreateDialobProps';
import { useUtilityClasses, FsDirentCreateDialobRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';


export const FsDirentCreateDialob: React.FC<FsDirentCreateDialobProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();
  const [technicalId, setTechnicalId] = React.useState('');

  return (
    <FsDirentCreateDialobRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntCreate.dialob.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.dialob.formNameField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.direntCreate.dialob.formNameField.placeholder' })} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.dialob.formTechnicalIdField.label' })}</Typography>
        <FsDirentTextField required={true}
          value={technicalId}
          onChange={setTechnicalId}
          placeholder={intl.formatMessage({ id: 'fs.direntCreate.dialob.formTechnicalIdField.placeholder' })}
        />

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentCreateDialobRoot>
  );
};

