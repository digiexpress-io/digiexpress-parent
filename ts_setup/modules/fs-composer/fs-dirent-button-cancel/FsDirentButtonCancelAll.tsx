import React from 'react';
import { useIntl } from 'react-intl';
import { FsDirentButtonCancelProps } from './FsDirentButtonCancelProps';
import { useUtilityClasses, FsDirentButtonCancelAllRoot } from './useUtilityClasses';
import { FsIcon, FsIcons } from '../fs-theme';

export const FsDirentButtonCancelAll: React.FC<FsDirentButtonCancelProps> = (props) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <FsDirentButtonCancelAllRoot
      className={classes.root}
      onClick={props.onClick}
      disabled={props.disabled}
      startIcon={<FsIcon icon={FsIcons.Close} small />}
    >
      {intl.formatMessage({ id: 'button.cancelAll' })}
    </FsDirentButtonCancelAllRoot>
  );
};
