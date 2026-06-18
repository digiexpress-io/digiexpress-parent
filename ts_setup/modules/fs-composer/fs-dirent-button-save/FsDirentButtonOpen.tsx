import React from 'react';
import { useIntl } from 'react-intl';
import { FsDirentButtonOpenProps } from './FsDirentButtonOpenProps';
import { useUtilityClasses, FsDirentButtonOpenRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

export const FsDirentButtonOpen: React.FC<FsDirentButtonOpenProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentButtonOpenRoot className={classes.root} ownerState={ownerState} onClick={props.onClick} disabled={props.disabled}>
      {intl.formatMessage({ id: 'button.view' })}
    </FsDirentButtonOpenRoot>
  );
};
