import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentFormField, FsDirentTextField } from '../fs-utilities';
import { useUtilityClasses, FsDirentFolderRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentFolderProps } from './FsDirentFolderProps';


export const FsDirentFolderUpdate: React.FC<FsDirentFolderProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentFolderRoot className={classes.root}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.folder.sectionTitle.edit' }, { name: ownerState.assetPath })}</Typography>
      <div className={classes.formContainer}>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.folder.locationField.label' })}>
          <FsDirentTextField value={ownerState.location} disabled />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.nameField.label' })}>
          <FsDirentTextField
            value={ownerState.name}
            placeholder={intl.formatMessage({ id: 'fs.dirent.folder.nameField.placeholder' })}
            onChange={ownerState.onChangeName}
          />
        </FsDirentFormField>


      </div>
    </FsDirentFolderRoot>
  );
};
