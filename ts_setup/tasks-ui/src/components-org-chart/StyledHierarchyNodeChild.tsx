import React from 'react';

const style: React.CSSProperties = {
  textAlign: 'center',
  verticalAlign: 'top',
  padding: '0',
};

export const StyledHierarchyNodeChild: React.FC<{
  children: React.ReactNode[],
  colspan: { node: number }
}> = ({ children, colspan }) => {

  return (<tr>
    {children.map((child, index) => (<td className='StyledHierarchyNodeChild' colSpan={colspan.node} key={index} style={style}> {child}</td>))}
  </tr>);
}