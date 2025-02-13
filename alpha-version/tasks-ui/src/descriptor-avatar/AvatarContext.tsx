import React from 'react';

import LoggerFactory from 'logger';
import { useBackend } from 'descriptor-backend';
import { AvatarStore, AvatarContextType, Avatar, Avatars } from './avatar-types';

const log = LoggerFactory.getLogger();

export const AvatarContext = React.createContext<AvatarContextType>({} as any);

export const AvatarProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const backend = useBackend();
  const [avatars, setAvatars] = React.useState<Avatars>(Object.freeze({}));

  const withAvatars = React.useCallback(async (all: string[]) => {
    const response = await new AvatarStore(backend.store).findAvatars(all);
    setAvatars(prev => {
      const next: Record<string, Avatar> = {...prev};
      response.forEach(avatar => next[avatar.id] = avatar)
      response.forEach(avatar => next[avatar.externalId] = avatar)
      return Object.freeze(next);
    })
  }, [setAvatars]);
  
  const contextValue: AvatarContextType = React.useMemo(() => {
    return { withAvatars, avatars } 
  }, [avatars, withAvatars]);
  
  return (<AvatarContext.Provider value={contextValue}>{children}</AvatarContext.Provider>);
}

export function useAvatars(entries: string[]): Avatar[] | undefined {
  const ctx: AvatarContextType = React.useContext(AvatarContext);
  const mapped: Avatar[] = entries.map(entry => ctx.avatars[entry]).filter(entry => !!entry)
  const calculated = mapped.length === entries.length ? mapped : undefined;

  React.useEffect(() => {
    if(calculated === undefined) {
      ctx.withAvatars(entries);
    }
  }, [calculated, entries]);

  return calculated;
}

export function useAvatar(entry: string | undefined, enabled?: boolean): Avatar | undefined {
  const ctx: AvatarContextType = React.useContext(AvatarContext);
  const calculated: Avatar | undefined = entry ? ctx.avatars[entry] : undefined;

  React.useEffect(() => {
    if(enabled === false || !entry) {
      return;
    }

    if(calculated === undefined) {
      ctx.withAvatars([entry]);
    }
  }, [calculated, entry, enabled]);

  return calculated;
}