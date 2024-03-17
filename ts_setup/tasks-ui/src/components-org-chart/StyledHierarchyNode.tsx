import React from 'react';
import { ChartNode } from './org-chart-types';

const boxStyles: React.CSSProperties = {
  border: "1px solid #e5e7eb",
  background: "#ffffff",
  color: "#4b5563",
  padding: "1.25rem",
  borderColor: "red",
  borderRadius: "0.25rem",
  display: "inline-block",

}

export const StyledHierarchyNode: React.FC<{
  node: ChartNode,
  children: React.ReactNode,
}> = ({ children, node }) => {

  const colSpan = node.node + node.left + node.right;
  return (
    <tr style={{
      display: "table-row"
    }}>
      <td colSpan={colSpan} style={{ boxSizing: "border-box", textAlign: "center" }}>
        <div style={boxStyles}>
          {children}
        </div>
      </td>
    </tr>);
}