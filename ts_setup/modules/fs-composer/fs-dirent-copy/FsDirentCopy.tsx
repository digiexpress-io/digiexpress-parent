import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentTextField } from '../fs-utilities';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentCopyProps } from './FsDirentCopyProps';
import { useOwnerState } from './useOwnerState';
import { useUtilityClasses, FsDirentCopyRoot } from './useUtilityClasses';

export const FsDirentCopy: React.FC<FsDirentCopyProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentCopyRoot className={classes.root}>
      <div className={classes.titleRow}>
        <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntCopy.title' })}</Typography>
        <FsDirentButtonSave onClick={ownerState.onSave} disabled={!ownerState.isDirty} />
      </div>

      <FsDirentTextField value={ownerState.name}
        onChange={(value) => ownerState.onChangeName(value)}
        placeholder={intl.formatMessage({ id: 'fs.direntCopy.placeholder' })}
      />
    </FsDirentCopyRoot>
  );
};
