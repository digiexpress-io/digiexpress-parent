import React from 'react';
import { TextField, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentTextFieldProps } from './FsDirentTextFieldProps';
import { useUtilityClasses, FsDirentTextFieldRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

export const FsDirentTextField: React.FC<FsDirentTextFieldProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentTextFieldRoot className={classes.root} ownerState={ownerState}>
      <TextField size='small' fullWidth
        placeholder={props.placeholder}
        value={props.value}
        disabled={props.disabled}
        multiline={props.multiline}
        minRows={props.minRows}
        maxRows={props.maxRows}
        onChange={(e) => props.onChange?.(e.target.value)}
      />
      {ownerState.showRequiredError && (
        <Typography className={classes.requiredMessage}>
          {intl.formatMessage({ id: 'fs.direntTextField.required' })}
        </Typography>
      )}
    </FsDirentTextFieldRoot>
  );
};
