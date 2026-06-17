import { DateTime } from 'luxon';
import { useFsDirent } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { FsPanelHistoryProps } from './FsPanelHistoryProps';


export interface OwnerState {
  isDarkMode: boolean;
  direntName: string;
  createdAt: string | undefined;
  createdBy: string | undefined;
  updatedAt: string | undefined;
  updatedBy: string | undefined;
}

function _formatDate(value: string | undefined): string | undefined {
  if (!value) {
    return undefined;
  }
  const dt = DateTime.fromISO(value);
  return dt.isValid ? dt.toFormat('d.M.yyyy HH:mm') : undefined;
}

export const useOwnerState = (props: FsPanelHistoryProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirentName } = useFsDirent();

  const index = props.dirent!.commitIndex;

  return {
    isDarkMode,
    direntName: getDirentName(props.dirent!.id) ?? props.dirent!.name,
    createdAt: _formatDate(index?.createdAt),
    createdBy: index?.createdByAuthor,
    updatedAt: _formatDate(index?.updatedAt),
    updatedBy: index?.updatedByAuthor,
  };
};
