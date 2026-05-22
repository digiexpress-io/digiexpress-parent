import React from 'react';
import { useIntl } from 'react-intl';
import { FsDirentButtonSaveProps } from './FsDirentButtonSaveProps';
import { useUtilityClasses, FsDirentButtonSaveRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

export const FsDirentButtonSave: React.FC<FsDirentButtonSaveProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentButtonSaveRoot className={classes.root} ownerState={ownerState} onClick={props.onClick}>
      {intl.formatMessage({ id: 'button.save' })}
    </FsDirentButtonSaveRoot>
  );
};
