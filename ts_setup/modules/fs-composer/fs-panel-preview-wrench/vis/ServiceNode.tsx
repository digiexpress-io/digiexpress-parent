import React from 'react';
import { Handle, Position } from '@xyflow/react';
import { useNode } from './vis-context';

export const ServiceNode = React.memo<{ id: string }>( ({ id }) => {
  const target = useNode(id);
  const label: string | undefined = target?.data?.label as any;

  return (
    <div id={id}>
      <div>{label || 'no node connected'}</div>
      <Handle type="source" position={Position.Bottom} />
      <Handle type="target" position={Position.Top} />
    </div>
  );
});
