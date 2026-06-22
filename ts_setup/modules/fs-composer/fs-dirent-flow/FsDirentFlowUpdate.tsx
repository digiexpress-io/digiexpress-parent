import React from 'react';

import { useUtilityClasses, FsDirentFlowRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentFlowProps } from './FsDirentFlowProps';
import { MonacoIntegration } from './MonacoIntegration';
import { FsDirentBodyProvider } from '@dxs-ts/fs-api';


export const FsDirentFlowUpdate: React.FC<FsDirentFlowProps> = (props) => {

  return (<FsDirentBodyProvider direntId={props.direntId}>
    <Internal {...props} />
  </FsDirentBodyProvider>);
}

const Internal: React.FC<FsDirentFlowProps> = (props) => {
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentFlowRoot className={classes.root}>
      <MonacoIntegration
        id={ownerState.id}
        key={ownerState.id}
        src={ownerState.content}
        onChange={ownerState.onChangeContent}
        flow={ownerState.flow}
        messages={ownerState.flow.errors}
      />
    </FsDirentFlowRoot>
  );
}