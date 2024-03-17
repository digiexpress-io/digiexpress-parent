import React from 'react';

import { ChartNode } from './org-chart-types';


const rowStyle: React.CSSProperties = {
  height: '30px'
}
const lineStyle: React.CSSProperties = {
  background: "green",
  margin: "0 auto",
  height: "20px",
  width: "1px",
  boxSizing: "border-box"
}

export const StyledHierarchyNodeLinesDown: React.FC<{
  node: ChartNode;
  children: any[] | undefined;
}> = ({ node, children }) => {
  if (!children || children.length === 0) {
    return null;
  }

  const colSpan = node.node + node.left + node.right;
  return (
    <tr style={rowStyle}>
      <td colSpan={colSpan}>
        <div style={lineStyle}></div>
      </td>
    </tr>
  );

}