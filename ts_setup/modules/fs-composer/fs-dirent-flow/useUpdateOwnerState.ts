import React from 'react';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  name: string;
  content: string;
  wrenchBody: Fs.WrenchBody | undefined;
  onChangeName: (value: string) => void;
  onChangeContent: (value: string) => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent, fetchDirentBody } = useFsDirent();

  const dirent = getDirent(props.direntId);

  const [name, setName] = React.useState(dirent?.name ?? '');
  const [content, setContent] = React.useState('');
  const [wrenchBody, setWrenchBody] = React.useState<Fs.WrenchBody | undefined>(undefined);

  React.useEffect(() => {
    fetchDirentBody(props.direntId, 'FLOW').then((body) => {
      const wb = body as Fs.WrenchBody;
      const yaml = wb.flows[props.direntId]?.ast?.parseTree?.value ?? '';
      setWrenchBody(wb);
      setContent(yaml);
    });
  }, [props.direntId]);

  function onChangeName(value: string) {
    setName(value);
  }

  function onChangeContent(value: string) {
    setContent(value);
  }

  return ({
    isDarkMode,
    dirent,
    name,
    onChangeName,
    content,
    wrenchBody,
    onChangeContent,
  });
};
