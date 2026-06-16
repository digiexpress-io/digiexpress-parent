import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentLabelsProps } from './FsDirentLabelsProps';
import { useOwnerState } from './useOwnerState';
import { useUtilityClasses, FsDirentLabelsRoot } from './useUtilityClasses';
import { FsDirentTextFieldAutocomplete } from '../fs-dirent-textfield-autocomplete';
import { FsDirentButtonSave } from '../fs-dirent-button-save';

export const FsDirentLabels: React.FC<FsDirentLabelsProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentLabelsRoot className={classes.root} ownerState={ownerState}>
      <div className={classes.titleRow}>
        <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.labelsField.label' })}</Typography>
        <FsDirentButtonSave onClick={ownerState.onSave} disabled={!ownerState.isDirty} />
      </div>

      <FsDirentTextFieldAutocomplete
        options={ownerState.labelOptions}
        value={ownerState.labels}
        onChange={ownerState.onChangeLabels}
        placeholder={intl.formatMessage({ id: 'fs.dirent.labelsField.placeholder' })}
      />
    </FsDirentLabelsRoot>
  );
};
