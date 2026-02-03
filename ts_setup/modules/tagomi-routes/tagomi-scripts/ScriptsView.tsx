import React from 'react';
import { ScriptsViewRoot, useUtilityClasses } from '../tagomi-explorer/script/useUtilityClasses';
import { ScriptsList } from '../tagomi-explorer/script/ScriptsList';

export const ScriptsView: React.FC = () => {
  const classes = useUtilityClasses();

  return (
    <ScriptsViewRoot className={classes.root}>
      <ScriptsList />
    </ScriptsViewRoot>)
}


