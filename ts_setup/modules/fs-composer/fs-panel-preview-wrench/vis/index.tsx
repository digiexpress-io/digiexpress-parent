import React from 'react';
import { ReactFlowProvider } from '@xyflow/react';

import { Styles } from './Styles';
import { Internal } from './Internal';
import { Model, Node, Edge, VisProps } from './vis-types';



export type { Model, Node, Edge, VisProps };

export const Vis: React.FC<VisProps> = (init) => {
  const Render = React.useCallback((props: VisProps) => (<ReactFlowProvider><Internal {...props} /></ReactFlowProvider>), [init.id])
  return (<Styles><Render {...init} /></Styles>);
}
