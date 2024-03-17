import React from 'react';

const style: React.CSSProperties = {
  borderSpacing: 0,
  borderCollapse: 'separate',
  margin: '0 auto'
}

export const StyledHierarchy: React.FC<{ id: string, children: React.ReactNode }> = ({ id, children }) => {
  return (
    <table key={id} style={style}>
      {children}
    </table>)
}