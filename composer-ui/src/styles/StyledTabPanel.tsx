import React from "react";


export interface TabPanelProps {
  children?: React.ReactNode;
  dir?: string;
  index: number;
  value: number;
}


export const StyledTabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => {
  return (<div hidden={value !== index} id={`${index}`}>{children}</div>
  );
}
