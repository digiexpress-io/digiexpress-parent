import React from 'react';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { FsPanelPreviewWrenchProps } from './FsPanelPreviewWrenchProps';


export interface OwnerState {
  isDarkMode: boolean;
  flowAst: Fs.FlowAst | undefined;
  wrenchBody: Fs.WrenchBody | undefined;
}

export const useOwnerState = (props: FsPanelPreviewWrenchProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  const { fetchDirentBody } = useFsDirent();

  const [wrenchBody, setWrenchBody] = React.useState<Fs.WrenchBody | undefined>(undefined);

  React.useEffect(() => {
    fetchDirentBody(props.dirent.id, 'FLOW')
      .then(body => setWrenchBody(body as Fs.WrenchBody));
  }, [props.dirent.id]);

  const flowAst = wrenchBody?.flows[props.dirent.id]?.ast;

  return { isDarkMode, flowAst, wrenchBody };
};
