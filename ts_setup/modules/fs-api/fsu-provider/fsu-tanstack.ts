import { Store } from '@tanstack/react-store';

import { FsuChange } from "./fsu-types";
import { FsuWorld } from './FsuWorld';

// ─── Store state ────────────────────────────────────────────────────────────

export interface FsuStoreState {
  world: FsuWorld;
}

// ─── Create the store ────────────────────────────────────────────────────────

export const fsuStore = new Store<FsuStoreState>({ world: new FsuWorld() });

// ─── Action helpers ──────────────────────────────────────────────────────────
// Each action replaces world with the immutable result from FsuWorld methods.

export const fsuActions = {
  /**
   * Register a brand-new change node.
   * Returns the new change's id.
   */
  newChange(init: () => FsuChange): string {
    let createdId = "";
    fsuStore.setState((prev) => {
      const [nextWorld, id] = prev.world.withNewChange(init);
      createdId = id;
      return { world: nextWorld };
    });
    return createdId;
  },

  /**
   * Mutate an existing change node.
   */
  updateChange<T extends FsuChange>(
    id: string,
    callback: (prev: T) => T
  ): void {
    fsuStore.setState((prev) => ({
      world: prev.world.withChange<T>(id, callback),
    }));
  },

  /**
   * Remove a change node.
   */
  clearChange(id: string): void {
    fsuStore.setState((prev) => ({
      world: prev.world.clearChange(id),
    }));
  },

  /**
   * Replace the entire world (e.g. after a server sync).
   */
  replaceWorld(world: FsuWorld): void {
    fsuStore.setState(() => ({ world }));
  },
};