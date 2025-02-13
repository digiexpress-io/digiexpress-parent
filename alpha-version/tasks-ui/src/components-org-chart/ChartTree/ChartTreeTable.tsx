import React from 'react';


import { createChartNode } from './org-chart-client';
import { ChartProps } from './org-chart-types';
import { StyledHierarchy } from './StyledHierarchy';
import { StyledHierarchyNode } from './StyledHierarchyNode';
import { StyledHierarchyNodeChild } from './StyledHierarchyNodeChild';
import { StyledHierarchyNodeLinesDown } from './StyledHierarchyNodeLinesDown';
import { StyledHierarchyNodeLinesToChildren } from './StyledHierarchyNodeLinesToChildren';



export const ChartTree: React.FC<{ value: ChartProps, parent?: ChartProps, index?: number }> = (props) => {
  const node = createChartNode(props.value);

  return (
    <StyledHierarchy id={props.value.id}>
      <tbody>
        <StyledHierarchyNode node={node}>{props.value.label}</StyledHierarchyNode>
        <StyledHierarchyNodeLinesDown node={node} children={props.value.children} />
        <StyledHierarchyNodeLinesToChildren node={node} />

        <StyledHierarchyNodeChild colspan={node}>
          {(props.value.children ?? []).map((child, index) => <ChartTree key={index} parent={props.value} value={child} index={index} />)}
        </StyledHierarchyNodeChild>
      </tbody>
    </StyledHierarchy>)
}

