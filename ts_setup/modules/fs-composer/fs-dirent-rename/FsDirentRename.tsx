import React from 'react';
import { TextField, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentRenameProps } from './FsDirentRenameProps';
import { useUtilityClasses, FsDirentRenameRoot } from './useUtilityClasses';

export const FsDirentRename: React.FC<FsDirentRenameProps> = (props) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <FsDirentRenameRoot className={classes.root}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntRename.title' })}</Typography>
      <TextField
        className={classes.textField}
        placeholder={props.dirent?.name}
        size='small'
        fullWidth
      />
    </FsDirentRenameRoot>
  );
};
