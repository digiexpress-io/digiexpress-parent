import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentLabelsProps } from './FsDirentLabelsProps';
import { useOwnerState } from './useOwnerState';
import { useUtilityClasses, FsDirentLabelsRoot } from './useUtilityClasses';
import { FsDirentTextFieldAutocomplete } from '../fs-dirent-textfield-autocomplete';

export const FsDirentLabels: React.FC<FsDirentLabelsProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentLabelsRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.labelsField.label' })}</Typography>
      <FsDirentTextFieldAutocomplete
        options={['']}
        value={['todo']}
        onChange={() => { }}
        placeholder={intl.formatMessage({ id: 'fs.dirent.labelsField.placeholder' })}
      />
    </FsDirentLabelsRoot>
  );
};
