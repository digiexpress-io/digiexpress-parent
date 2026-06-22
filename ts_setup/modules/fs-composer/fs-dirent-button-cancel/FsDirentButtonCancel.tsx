import React from 'react';
import { useIntl } from 'react-intl';
import { FsDirentButtonCancelProps } from './FsDirentButtonCancelProps';
import { useUtilityClasses, FsDirentButtonCancelRoot } from './useUtilityClasses';

export const FsDirentButtonCancel: React.FC<FsDirentButtonCancelProps> = (props) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <FsDirentButtonCancelRoot className={classes.root} onClick={props.onClick}>
      {intl.formatMessage({ id: 'button.cancel' })}
    </FsDirentButtonCancelRoot>
  );
};
