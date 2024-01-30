import React from 'react';


import { AvatarContextType, AvatarReducer, Avatar } from './avatar-types';
import { initAvatars } from './initMethods';
import LoggerFactory from 'logger';

const log = LoggerFactory.getLogger();

export const AvatarContext = React.createContext<AvatarContextType>({} as any);

export const AvatarProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [avatars, setAvatars] = React.useState(initAvatars());

  const withAvatar = React.useCallback((value: string) => setAvatars(prev => prev.withAvatar(value)), [setAvatars]);
  const withAvatars = React.useCallback((all: string[]) => setAvatars(prev => prev.withAvatars(all)), [setAvatars]);
  
  const contextValue: AvatarContextType = React.useMemo(() => {
    const reducer = { withAvatars, withAvatar };
    return { reducer, avatars } 
  }, [avatars, withAvatars]);
  

  return (<AvatarContext.Provider value={contextValue}>{children}</AvatarContext.Provider>);
}

export function useAvatars(entries: string[]): Avatar[] | undefined {
  const ctx: AvatarContextType = React.useContext(AvatarContext);
  let mapped = entries.map(entry => ctx.avatars.values[entry]).filter(entry => !!entry)
  const calculated = mapped.length === entries.length ? mapped : undefined;

  React.useEffect(() => {
    if(calculated === undefined) {
      ctx.reducer.withAvatars(entries);
    }
  }, [calculated, entries]);

  return calculated;
}

export function useAvatar(entry: string): Avatar | undefined {
  const ctx: AvatarContextType = React.useContext(AvatarContext);
  const calculated = ctx.avatars.values[entry];

  React.useEffect(() => {
    if(calculated === undefined) {
      ctx.reducer.withAvatar(entry);
    }
  }, [calculated, entry]);

  return calculated;
}