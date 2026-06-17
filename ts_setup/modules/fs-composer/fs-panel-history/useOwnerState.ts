import { DateTime } from 'luxon';
import { useFsDirent } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { FsPanelHistoryProps } from './FsPanelHistoryProps';


export interface OwnerState {
  isDarkMode: boolean;
  direntName: string;
  createdAt: string;
  createdBy: string;
  updatedAt: string | undefined;
  updatedBy: string | undefined;
}

export const useOwnerState = (props: FsPanelHistoryProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirentName } = useFsDirent();

  const index = props.dirent!.commitIndex!;

  return {
    isDarkMode,
    direntName: getDirentName(props.dirent!.id) ?? props.dirent!.name,
    createdAt: DateTime.fromISO(index.createdAt).toFormat('d.M.yyyy HH:mm'),
    createdBy: index.createdByAuthor,
    updatedAt: index.updatedAt ? DateTime.fromISO(index.updatedAt).toFormat('d.M.yyyy HH:mm') : undefined,
    updatedBy: index.updatedByAuthor,
  };
};
