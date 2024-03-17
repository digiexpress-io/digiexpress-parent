import React from 'react';
import { ChartNode, BorderShape } from './org-chart-types';

const rowStyle: React.CSSProperties = {
  height: '30px'
}

const cellStyleRight: React.CSSProperties = {
  borderRight: '1px solid pink',
}
const cellStyleTop: React.CSSProperties = {
  borderTop: '1px solid pink',
}
export const Borders: React.FC<{ index: number, shapes: BorderShape[] }> = ({ shapes }) => {
  return (<td style={{
    padding: '0 0.75rem',
    ...shapes.includes("BORDER_TOP") ? cellStyleTop : {},
    ...shapes.includes("BORDER_RIGHT") ? cellStyleRight : {},
  }}>&nbsp;</td>);
}

export const StyledHierarchyNodeLinesToChildren: React.FC<{ node: ChartNode }> = ({ node }) => {

  return (<tr style={rowStyle} className='StyledHierarchyNodeLinesToChildren'>
    {node.children.map((cell, key) => <Borders key={key} index={key} shapes={cell.shapes} />)}
  </tr>);
}