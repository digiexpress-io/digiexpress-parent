import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentCreateFolderProps } from './FsDirentCreateFolderProps';
import { useUtilityClasses, FsDirentCreateFolderRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

export const FsDirentCreateFolder: React.FC<FsDirentCreateFolderProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentCreateFolderRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntCreate.folder.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.folder.locationField.label' })}</Typography>
        <FsDirentTextField value={ownerState.locationPath || intl.formatMessage({ id: 'fs.direntCreate.folder.locationField.root' })} disabled />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.folder.nameField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.direntCreate.folder.nameField.placeholder' })} />

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentCreateFolderRoot>
  );
};
