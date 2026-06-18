import React from 'react';
import MonacoReact from '@monaco-editor/react';
import { useUtilityClasses, FsDirentFlowRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentFlowProps } from './FsDirentFlowProps';


export const FsDirentFlowUpdate: React.FC<FsDirentFlowProps> = (props) => {
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentFlowRoot className={classes.root} ownerState={ownerState}>
      <MonacoReact height="100vh" value={ownerState.content}
        defaultLanguage="yaml"
        onChange={(value) => ownerState.onChangeContent(value ?? '')}
        options={{
          wordBasedSuggestions: 'off',
          minimap: { enabled: false }
        }}
      />
    </FsDirentFlowRoot>
  );
};
