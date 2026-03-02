import { ConfigOption, FsNode } from "@dxs-ts/fs-api";



export interface FsConfigOptionsProps {
  node: FsNode | undefined;
}

export const allConfigOptions: (keyof ConfigOption)[] = ['devMode', 'disabledMode', 'anonymousMode', 'assignableMode'];
