import React from 'react';

export interface GroupContextType {
  level: number;
}

const context = React.createContext<GroupContextType>({level: 1});
export const GroupContext = context;
