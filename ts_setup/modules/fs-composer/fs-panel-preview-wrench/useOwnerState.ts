import React from 'react';
import { Fs, useFsDirent, useFsNav } from '@dxs-ts/fs-api';
import { FsPanelPreviewWrenchProps } from './FsPanelPreviewWrenchProps';


export interface OwnerState {
  isDarkMode: boolean;
  flowAst: any;
  wrenchBody: Fs.WrenchBody | undefined;
}

export const useOwnerState = (props: FsPanelPreviewWrenchProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  const { fetchDirentBody } = useFsDirent();

  const [wrenchBody, setWrenchBody] = React.useState<Fs.WrenchBody | undefined>(undefined);

  React.useEffect(() => {
    fetchDirentBody(props.dirent.id, 'FLOW')
      .then(body => setWrenchBody(body as Fs.WrenchBody));
  }, [props.dirent.id]);

  const flowAst = wrenchBody ? (wrenchBody.flows[props.dirent.id] as any)?.ast ?? undefined : undefined;

  return { isDarkMode, flowAst, wrenchBody };
};
