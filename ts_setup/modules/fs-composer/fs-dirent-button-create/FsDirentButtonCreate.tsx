import React from 'react';
import { useIntl } from 'react-intl';
import { FsDirentButtonCreateProps } from './FsDirentButtonCreateProps';
import { useUtilityClasses, FsDirentButtonCreateRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

export const FsDirentButtonCreate: React.FC<FsDirentButtonCreateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentButtonCreateRoot className={classes.root} ownerState={ownerState} onClick={props.onClick}>
      {intl.formatMessage({ id: 'button.save' })}
    </FsDirentButtonCreateRoot>
  );
};
