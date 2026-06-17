import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentFormField, FsDirentTextField } from '../fs-utilities';
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

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.folder.locationField.label' })}>
          <FsDirentTextField value={`${ownerState.locationPath ? `${ownerState.locationPath} / ` : ''}${intl.formatMessage({ id: 'fs.dirent.folder.locationField.newFolder' })}`} disabled />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.nameField.label' })}>
          <FsDirentTextField required placeholder={intl.formatMessage({ id: 'fs.dirent.folder.nameField.placeholder' })}
            value={ownerState.name}
            onChange={ownerState.onChangeName}
          />
        </FsDirentFormField>


      </div>
    </FsDirentFolderRoot>
  );
};
