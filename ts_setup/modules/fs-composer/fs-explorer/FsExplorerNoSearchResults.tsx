import React from 'react';
import { useIntl } from 'react-intl';
import { useUtilityClasses } from './useUtilityClasses';

export const FsExplorerNoSearchResults: React.FC = () => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <div className={classes.noSearchResults}>
      {intl.formatMessage({ id: 'fs.explorer.message.noResults' })}
    </div>
  );
};