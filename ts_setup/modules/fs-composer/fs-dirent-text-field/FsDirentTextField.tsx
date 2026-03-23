import React from 'react';
import { FsDirentTextFieldProps } from './FsDirentTextFieldProps';
import { useUtilityClasses, FsDirentTextFieldRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

export const FsDirentTextField: React.FC<FsDirentTextFieldProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentTextFieldRoot
      className={classes.root}
      ownerState={ownerState}
      size='small'
      fullWidth
      placeholder={props.placeholder}
      value={props.value}
      disabled={props.disabled}
      multiline={props.multiline}
      minRows={props.minRows}
      maxRows={props.maxRows}
    />
  );
};
