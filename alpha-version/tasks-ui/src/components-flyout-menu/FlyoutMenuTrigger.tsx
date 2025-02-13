import React from 'react';
import { useFlyoutMenu } from './FlyoutMenuContext';


export const FlyoutMenuTrigger: React.FC<{ 
  children: React.ReactNode;
}> = ({ children }) => {

  const ctx = useFlyoutMenu();
  return (<div onClick={ctx.onOpen}>{children}</div>);
}
