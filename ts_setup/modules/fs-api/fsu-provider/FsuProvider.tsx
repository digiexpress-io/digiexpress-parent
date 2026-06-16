import React from 'react';
import { useSelector } from '@tanstack/react-store';
import { Store } from '@tanstack/react-store';

import { FsuWorld } from './FsuWorld';
import { FsuChange, FsuCreateChange } from './fsu-types';
import { Fs } from '../fs-types';
import { useFsDirent } from '../fs-provider';

export interface FsuContextType {
  allChanges: FsuChange[];
  getChange(id: string): FsuChange;
  isChange(id: string): boolean;
  useChangeState<T extends FsuChange = FsuChange>(id: string): T;
  withNewChange<T extends FsuChange>(id: string, init: () => T): T;
  withChange<T extends FsuChange>(id: string, callback: (prev: T) => T): T;
  push(changeId: string): Promise<void>;
  pushCreate(change: FsuCreateChange): Promise<Fs.DirentBase>;
  cancel(changeId: string): void;
}

export interface FsuChangeContextType<T extends FsuChange> {
  state: T;
  isDirty: boolean;
  push(): Promise<void>;
  cancel(): void;
  update(callback: (prev: T) => T): T;
}

const store = new Store<FsuWorld>(new FsuWorld());


export function useFsu(): FsuContextType {

  const fs = useFsDirent();

  // Subscribe to the store so the context value re-derives on every world change.
  const world = useSelector(store);

  const contextValue: FsuContextType = React.useMemo(() => {

    function withChange<T extends FsuChange>(id: string, callback: (prev: T) => T): T {
      const nextWorld = world.withChange(id, callback);
      store.setState(() => nextWorld);
      return nextWorld.getChange(id) as T;
    }
    return {
      allChanges: world.allChanges,
      getChange: (id) => world.getChange(id),
      isChange: (id) => world.isChange(id),
      withNewChange<T extends FsuChange>(id: string, init: () => T): T {
        if (world.isChange(id)) {
          return world.getChange(id) as T;
        }
        const [nextWorld] = world.withNewChange(init);
        store.setState(() => nextWorld);
        return nextWorld.getChange(id) as T;
      },
      withChange,
      useChangeState<T extends FsuChange = FsuChange>(id: string): T {
        // eslint-disable-next-line react-hooks/rules-of-hooks
        return useSelector(store, (w) =>
          w.isChange(id) ? (w.getChange(id) as T) : undefined
        )!;
      },
      push: async (changeId) => {
        const change = world.getChange(changeId);
        await fs.updateDirent(change);
        store.setState((prev) => prev.clearChange(changeId));
      },
      pushCreate: async (change) => {
        return fs.createDirent(change);
      },
      cancel: (changeId) => {
        store.setState((prev) => prev.clearChange(changeId));
      },
    };
  }, [world, fs, store]);
  return contextValue;
}


export function useFsuIsChanged(
  id: string
): boolean {
  // subscribe only to this node
  const state = useSelector(store, (w) => w.isChange(id) && w.getChange(id).isDirty);
  return React.useMemo(() => state, [state, id]);
}


export function useFsuChange<T extends FsuChange>(
  id: string,
  init: () => T
): FsuChangeContextType<T> {

  const fs = useFsDirent();
  const initRef = React.useRef<T | null>(null);

  const initOnce = React.useCallback((): T => {
    if (initRef.current !== null) {
      return initRef.current;
    }
    initRef.current = init();
    return initRef.current;
  }, []);

  // register if not yet registered
  const world = store.state;
  React.useEffect(() => {
    if (!world.isChange(id)) {
      const [nextWorld] = world.withNewChange(initOnce);
      store.setState(() => nextWorld);
    }
  }, []);

  // subscribe only to this node
  const state = useSelector(store, (w) => w.findChange(id) as T | undefined) ?? initOnce();

  return React.useMemo(() => ({
    state,
    isDirty: state.isDirty,
    update(callback: (prev: T) => T): T {
      const nextWorld = store.state.withChange(id, callback);
      store.setState(() => nextWorld);
      return nextWorld.getChange(id) as T;
    },
    push: async () => {
      await fs.updateDirent(state!);
      store.setState((prev) => prev.clearChange(id));
    },
    cancel: () => {
      store.setState((prev) => prev.clearChange(id));
    },
  }), [state, fs, id]);
}