import React from 'react';

export const GDivider: React.FC<{
  total: number,
  index: number,
  children: React.ReactNode
}> = ({ total, index, children }) => {

  if (total > 1 && index < total - 1) {
    return (<>{children}</>)
  }

  return <></>;
}
