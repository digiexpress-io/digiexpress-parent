import React from 'react';
import {
  ReactFlow,
  useReactFlow,
  useNodesState,
  useEdgesState,
  OnNodesChange,
  Node,
  Edge
} from '@xyflow/react';

import { StartNode } from './StartNode';
import { EndNode } from './EndNode';
import { elkLayout } from './elk-layout';
import { VisProps } from './vis-types';
import { DecisionNode } from './DecisionNode';
import { SwitchNode } from './SwitchNode';
import { ServiceNode } from './ServiceNode';


export const Internal: React.FC<VisProps> = (init) => {
  const { model } = init;
  const { fitView } = useReactFlow();
  
  const [nodes, setNodes, onNodesChange] = useNodesState<Node>([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState<Edge>([]);

  
  const onLayout = React.useCallback(() => elkLayout(model)
    .then((layout) => {
      setNodes(layout.nodes);
      setEdges(layout.edges);
      window.requestAnimationFrame(() => fitView());
    }), [model]);


  React.useLayoutEffect(() => {
    onLayout();
  }, [model]);

  const onNodeDoubleClick = (props: any) => {
    try {
      const children: HTMLCollection = props.target.children;
      const [ selected ] = children;
      const { id: target } = selected;
      if(target) {
        const entityId: string | undefined = (nodes.find(({id}) => id === target) as any)?.data?.externalId;
        if(entityId) {
          init.events.onDoubleClick(entityId);
        }
      }
    } catch(e) {
      console.log(e);
    }
  }


  return (<ReactFlow fitView 
      nodes={nodes} edges={edges}
      onNodesChange={onNodesChange}
      onEdgesChange={onEdgesChange}
      nodesDraggable={true}
      onNodeDoubleClick={onNodeDoubleClick}
      nodeTypes={{
        start: StartNode,
        end: EndNode,
        decisionTable: DecisionNode,
        'switch':  SwitchNode,
        service: ServiceNode,
        flow: ServiceNode,
      }}
    />);
}


