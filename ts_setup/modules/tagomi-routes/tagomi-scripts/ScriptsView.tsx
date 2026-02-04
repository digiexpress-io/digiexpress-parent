import React from 'react';
import { ScriptsViewRoot, useScriptUtilityClasses, ScriptsList } from '../tagomi-explorer';

export const ScriptsView: React.FC = () => {
  const classes = useScriptUtilityClasses();

  return (
    <ScriptsViewRoot className={classes.root}>
      <ScriptsList />
    </ScriptsViewRoot>)
}


