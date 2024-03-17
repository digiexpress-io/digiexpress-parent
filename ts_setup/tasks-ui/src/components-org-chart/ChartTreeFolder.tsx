import React from 'react';
import { ChartProps } from './org-chart-types';


export const ChartTreeFolder: React.FC<{ value: ChartProps }> = ({ value: node }) => {
  return (
    <div key={node.label}>
      <span>{node.label}</span>
      {node.children && node.children.length > 0 && (
        <div style={{ marginLeft: '20px' }}>{node.children.map(c => <ChartTreeFolder value={c} />)}</div>
      )}
    </div>)
}

