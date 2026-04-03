import React from 'react';
import { Fs, useFsDirent, useFsNav } from '@dxs-ts/fs-api';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.Template | undefined;
  printoutServiceId: string;
  localeDisplay: string;
  content: string;
  onChangeContent: (value: string) => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent } = useFsDirent();

  const dirent = getDirent<Fs.Template>(props.direntId);
  const printout = dirent?.printoutServiceId ? getDirent<Fs.Printout>(dirent.printoutServiceId) : undefined;

  const localeId = dirent?.localeId ?? '';
  const intlLabel = printout?.intlValues?.[localeId];
  const localeDisplay = intlLabel ? `${localeId} - ${intlLabel}` : localeId;

  const [content, setContent] = React.useState(dirent?.content ?? '');

  function onChangeContent(value: string) {
    setContent(value);
  }

  return ({
    isDarkMode,
    dirent,
    printoutServiceId: dirent?.printoutServiceId ?? '',
    localeDisplay,
    content,
    onChangeContent,
  });
};
