import React from 'react';
import { useIntl } from 'react-intl';
import MonacoReact from '@monaco-editor/react';
import { useUtilityClasses, FsDirentFlowTaskRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentFlowTaskUpdateProps } from './FsDirentFlowTaskProps';


export const FsDirentFlowTaskUpdate: React.FC<FsDirentFlowTaskUpdateProps> = (props) => {
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentFlowTaskRoot className={classes.root} ownerState={ownerState}>

      <div className={classes.editor}>
        <MonacoReact
          height="100%"
          defaultLanguage="java"
          value={ownerState.taskValue}
          onChange={(v) => ownerState.onChangeTaskValue(v ?? '')}
          options={{
            wordBasedSuggestions: 'off',
            minimap: { enabled: true },
          }}
        />
      </div>

    </FsDirentFlowTaskRoot>
  );
};
