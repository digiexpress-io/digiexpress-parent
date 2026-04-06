import React from 'react';
import { useIntl } from 'react-intl';
import { FsDirentButtonCancelProps } from './FsDirentButtonCancelProps';
import { useUtilityClasses, FsDirentButtonCancelRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

export const FsDirentButtonCancel: React.FC<FsDirentButtonCancelProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentButtonCancelRoot className={classes.root} ownerState={ownerState} onClick={props.onClick}>
      {intl.formatMessage({ id: 'button.cancel' })}
    </FsDirentButtonCancelRoot>
  );
};
