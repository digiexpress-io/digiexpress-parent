import React from 'react';
import { Tooltip, Typography } from '@mui/material';
import { useFsuIsChanged } from '@dxs-ts/fs-api';
import { OwnerState } from './useOwnerState';
import { FsDiffIndicator } from '../fs-diff-indicator';


export interface FsTabItemProps {
  ownerState: OwnerState;
  className: string;
  index: number;
}


export const FsTab: React.FC<FsTabItemProps> = ({ ownerState, className, index }) => {
  const tab = ownerState.tabs[index];

  const isDirty = useFsuIsChanged(tab.direntId);

  return (
    <Tooltip title={tab.name} arrow enterDelay={700} placement="bottom">
      <span className={className}>
        {isDirty && <FsDiffIndicator direntId={tab.direntId} />}
        <Typography variant='subtitle2'>{tab.name}</Typography>
      </span>
    </Tooltip>
  )
}