import { EveliComponents } from '@dxs-ts/eveli-primitives'

export * from './wrench-setup'
export * from './wrench-sticky-save'
export * from './wrench-nav'

// @ts-ignore import module augmentation
declare module '@mui/material' {
  export interface Components<Theme = unknown> extends EveliComponents<Theme> { }
}

