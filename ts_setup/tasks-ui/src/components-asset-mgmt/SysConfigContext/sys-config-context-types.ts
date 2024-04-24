import { SysConfig } from 'descriptor-sys-config';
import { getInstance as createTabs} from 'descriptor-tabbing';

import Hdes from 'components-hdes/core';
import { StencilClient } from 'components-stencil';
export type TabTypes = 'current_config' | 'all_config';
export const Tabbing = createTabs<TabTypes, {}>();


export interface  SysConfigContextType {
  sysConfig: SysConfig | undefined;
  hdesSite: Hdes.Site | undefined; // available when backend is ready
  stencilSite: StencilClient.Release | undefined; // available when backend is ready

  loading: boolean; // is sys config loading
  reload(): Promise<void>;
}