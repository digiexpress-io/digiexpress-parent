import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentButtonDelete } from '../fs-dirent-button-delete';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentFolderRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentFolderUpdateProps } from './FsDirentFolderProps';


export const FsDirentFolderUpdate: React.FC<FsDirentFolderUpdateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentFolderRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.folder.sectionTitle.edit' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.folder.locationField.label' })}</Typography>
        <FsDirentTextField value={ownerState.location} disabled />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.nameField.label' })}</Typography>
        <FsDirentTextField
          value={ownerState.name}
          placeholder={intl.formatMessage({ id: 'fs.dirent.folder.nameField.placeholder' })}
          onChange={ownerState.onChangeName}
        />

        <div className={classes.buttonContainer}>
          <FsDirentButtonDelete assetId={props.direntId} />
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentFolderRoot>
  );
};
