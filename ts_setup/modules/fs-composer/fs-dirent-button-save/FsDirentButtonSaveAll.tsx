import React from 'react';
import { useIntl } from 'react-intl';
import { FsDirentButtonSaveProps } from './FsDirentButtonSaveProps';
import { useUtilityClasses, FsDirentButtonSaveAllRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';
import { FsIcon, FsIcons } from '../fs-theme';

export const FsDirentButtonSaveAll: React.FC<FsDirentButtonSaveProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentButtonSaveAllRoot
      className={classes.root}
      ownerState={ownerState}
      onClick={props.onClick}
      disabled={props.disabled}
      startIcon={<FsIcon icon={FsIcons.Checkmark} small />}
    >
      {intl.formatMessage({ id: 'button.saveAll' })}
    </FsDirentButtonSaveAllRoot>
  );
};
