import React from 'react';

import { FsuWorld, FsuChange, FsuCreateChange } from './FsuWorld';
import { Fs } from '../fs-types';
import { useFsDirent } from '../fs-provider';


export interface FsuContextType {

  allChanges: FsuChange[];
  getChange(id: string): FsuChange;
  isChange(id: string): boolean;
  withNewChange<T extends FsuChange>(id: string, init: () => T): T;
  withChange<T extends FsuChange>(id: string, callback: (prev: T) => T): T

  // pushes to backend
  push(changeId: string): Promise<void>;
  pushCreate(change: FsuCreateChange): Promise<Fs.DirentBase>;
  // discards local changes without saving
  cancel(changeId: string): void;
}

const FsuContext = React.createContext<FsuContextType | undefined>(undefined);

export interface FsuProviderProps {
  children: React.ReactNode;
}

export const FsuProvider: React.FC<FsuProviderProps> = (props) => {
  const fs = useFsDirent();

  const [fsu, setFsu] = React.useState<FsuWorld>(() => new FsuWorld());
  const contextValue: FsuContextType = React.useMemo(() => {
    function withNewChange<T extends FsuChange>(id: string, init: () => T) {
      if (fsu.isChange(id)) {
        return fsu.getChange(id) as T;
      }

      const [world] = fsu.withNewChange(init)
      setFsu(world);
      return world.getChange(id) as T;
    }

    function withChange<T extends FsuChange>(id: string, callback: (prev: T) => T)  {
      const world = fsu.withChange(id, callback)
      setFsu(world);
      return world.getChange(id) as T;
    }

    return {
      allChanges: fsu.allChanges,
      withNewChange,
      withChange,
      getChange: (id) => fsu.getChange(id),
      isChange: (id) => fsu.isChange(id),
      push: async (changeId) => {
        const change = fsu.getChange(changeId);
        await fs.updateDirent(change);
        setFsu(prev => prev.clearChange(changeId));
      },
      pushCreate: async (change) => {
        return fs.createDirent(change);
      },
      cancel: (changeId) => {
        setFsu(prev => prev.clearChange(changeId));
      }
    };
  }, [fsu]);

  return (
    <FsuContext.Provider value={contextValue}>
      {props.children}
    </FsuContext.Provider>
  );
}

export function useFsu(): FsuContextType {
  const result = React.useContext(FsuContext);
  if (!result) {
    throw new Error('FsuContext is not created!')
  }
  return result;
}
