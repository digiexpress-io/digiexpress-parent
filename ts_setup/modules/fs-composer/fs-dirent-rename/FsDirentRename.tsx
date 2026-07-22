import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentTextField } from '../fs-utilities';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentRenameProps } from './FsDirentRenameProps';
import { useOwnerState } from './useOwnerState';
import { useUtilityClasses, FsDirentRenameRoot } from './useUtilityClasses';

export const FsDirentRename: React.FC<FsDirentRenameProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentRenameRoot className={classes.root}>
      <div className={classes.titleRow}>
        <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntRename.title' })}</Typography>
        <FsDirentButtonSave onClick={ownerState.onSave} disabled={!ownerState.isDirty} />
      </div>

      <FsDirentTextField value={ownerState.name}
        onChange={(value) => ownerState.onChangeName(value)}
        placeholder={intl.formatMessage({ id: 'fs.direntRename.placeholder' })}
      />
    </FsDirentRenameRoot>
  );
};
