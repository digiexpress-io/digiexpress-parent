import React from 'react';


import { AvatarContextType, AvatarReducer, Avatar } from './avatar-types';
import { initAvatars, initReducer } from './initMethods';
import LoggerFactory from 'logger';

const log = LoggerFactory.getLogger();

export const AvatarContext = React.createContext<AvatarContextType>({} as any);

export const AvatarProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [avatars, setAvatars] = React.useState(initAvatars());
  const reducer: AvatarReducer = React.useMemo(() => initReducer(avatars, setAvatars), [avatars, setAvatars]);
  const contextValue: AvatarContextType = React.useMemo(() => ({ reducer, avatars }), [avatars, reducer]);
  
  React.useEffect(() => {
    log.code("AvatarProvider001").trace("reloading");
  }, [reducer]);

  React.useEffect(() => {
    log.code("AvatarProvider002").trace("reloading");
  }, [avatars]);

  return (<AvatarContext.Provider value={contextValue}>{children}</AvatarContext.Provider>);
}


function getAvatars(ctx: AvatarContextType, entries: string[]): Avatar[] | undefined {
  const result: Avatar[] = [];
  for(const letters of entries) {
    const avatar = ctx.avatars.values[letters];
    if(!avatar) {
      return undefined;
    }
    result.push(avatar);
  }
  return result;
}

export function useAvatars(entries: string[]): Avatar[] | undefined {
  const ctx: AvatarContextType = React.useContext(AvatarContext);
  const [avatars, setAvatars] = React.useState<Avatar[]>();

  React.useEffect(() => {
    if(!avatars) {
      return;
    }
    for(const avatar of avatars) {
      if(!entries.includes(avatar.origin)) {
        setAvatars(undefined);
        break;;
      }
    }
  }, [entries, avatars]);


  React.useEffect(() => {
    if(avatars === undefined) {
      const result = ctx.reducer.withAvatars(entries);
      setAvatars(result);
    }
  }, [avatars]);
  return avatars;
}

export function useAvatar(entry: string) {
  const ctx: AvatarContextType = React.useContext(AvatarContext);
  const [avatars, setAvatars] = React.useState(getAvatars(ctx, [entry])?.[0]);

  React.useEffect(() => {
    if(avatars?.origin !== entry) {
      setAvatars(undefined);
    }
  }, [entry, avatars]);


  React.useEffect(() => {
    if(avatars === undefined) {
      const result = ctx.reducer.withAvatar(entry);
      setAvatars(result);
    }
  }, [avatars]);
  
  return avatars;
}