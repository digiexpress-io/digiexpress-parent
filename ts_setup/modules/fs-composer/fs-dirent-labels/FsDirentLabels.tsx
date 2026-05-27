import React from 'react';
import { TextField } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentLabelsProps } from './FsDirentLabelsProps';
import { useOwnerState } from './useOwnerState';
import { useUtilityClasses, FsDirentLabelsRoot } from './useUtilityClasses';

export const FsDirentLabels: React.FC<FsDirentLabelsProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();
  const [labels, setLabels] = React.useState('');

  return (
    <FsDirentLabelsRoot className={classes.root} ownerState={ownerState}>
      <TextField className={classes.textField}
        multiline
        minRows={2}
        maxRows={5}
        value={labels}
        onChange={(e) => setLabels(e.target.value)}
        placeholder={intl.formatMessage({ id: 'fs.dirent.labelsField.placeholder' })}
        size='small'
      />
    </FsDirentLabelsRoot>
  );
};
