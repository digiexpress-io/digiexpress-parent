import React from 'react';
import { useUtilityClasses } from './useUtilityClasses';

export const FsExplorerNoSearchResults: React.FC = () => {
  const classes = useUtilityClasses();

  return (
    <div className={classes.noSearchResults}>
      No results found
    </div>
  );
};