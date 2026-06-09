import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentDescriptionProps } from './FsDirentDescriptionProps';
import { useOwnerState } from './useOwnerState';
import { useUtilityClasses, FsDirentDescriptionRoot } from './useUtilityClasses';


export const FsDirentDescription: React.FC<FsDirentDescriptionProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();
  const [description, setDescription] = React.useState('');

  return (
    <FsDirentDescriptionRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.descriptionField.label' })}</Typography>
      <FsDirentTextField multiline minRows={15} maxRows={40}
        value={description}
        onChange={(value) => setDescription(value)}
        placeholder={intl.formatMessage({ id: 'fs.dirent.descriptionField.placeholder' })}
      />
    </FsDirentDescriptionRoot>
  );
};
