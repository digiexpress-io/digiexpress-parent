import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentFolderRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';
import { FsDirentFolderCreateProps } from './FsDirentFolderProps';


export const FsDirentFolderCreate: React.FC<FsDirentFolderCreateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentFolderRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.folder.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.folder.locationField.label' })}</Typography>
        <FsDirentTextField value={`${ownerState.locationPath ? `${ownerState.locationPath} / ` : ''}${intl.formatMessage({ id: 'fs.dirent.folder.locationField.newFolder' })}`} disabled />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.nameField.label' })}</Typography>
        <FsDirentTextField required placeholder={intl.formatMessage({ id: 'fs.dirent.folder.nameField.placeholder' })}
          value={ownerState.name}
          onChange={ownerState.onChangeName}
        />

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel onClick={ownerState.onCancel} />
          <FsDirentButtonSave onClick={ownerState.onSave} disabled />
        </div>

      </div>
    </FsDirentFolderRoot>
  );
};
