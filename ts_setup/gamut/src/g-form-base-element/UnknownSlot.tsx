import React from 'react';


export interface UnknownSlotProps {
  id: string;
  children: React.ReactNode;
  element: any;
}


export const UnknownSlot: React.FC<UnknownSlotProps> = (props) => {
  console.groupCollapsed("unknown form base", props.id)
  console.error("Fill object", props);
  console.groupEnd();
  return (<div>
    <div>unknown = {props.id}</div>
    <div>{props.children}</div>
  </div>);
}

