import React from 'react';
import MonacoReact from '@monaco-editor/react';
import { useUtilityClasses, FsDirentFlowTaskRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentFlowTaskProps } from './FsDirentFlowTaskProps';
import { FsDirentBodyProvider } from '@dxs-ts/fs-api';


export const FsDirentFlowTaskUpdate: React.FC<FsDirentFlowTaskProps> = (props) => {
  return (<FsDirentBodyProvider direntId={props.direntId}>
    <Internal {...props} />
  </FsDirentBodyProvider>);
};


const Internal: React.FC<FsDirentFlowTaskProps> = (props) => {
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentFlowTaskRoot className={classes.root}>
      <MonacoReact height="100vh"
        defaultLanguage="java"
        value={ownerState.taskValue}
        onChange={(v) => ownerState.onChangeTaskValue(v ?? '')}
        options={{
          wordBasedSuggestions: 'off',
          minimap: { enabled: true },
        }}
      />
    </FsDirentFlowTaskRoot>
  );
};

