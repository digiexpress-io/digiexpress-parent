import React from 'react';
import { Tooltip, Typography } from '@mui/material';
import { useFsu } from '@dxs-ts/fs-api';
import { OwnerState } from './useOwnerState';
import { FsDiffIndicator } from '../fs-diff-indicator';


export interface FsTabItemProps {
  ownerState: OwnerState;
  className: string;
  index: number;
}


export const FsTab: React.FC<FsTabItemProps> = ({ ownerState, className, index }) => {
  const tab = ownerState.tabs[index];
  const { isChange, getChange } = useFsu();
  const isChanged = isChange(tab.id) && getChange(tab.id).isChanged;

  return (
    <Tooltip title={tab.name} arrow enterDelay={700} placement="bottom">
      <span className={className}>
        <Typography variant='subtitle2'>{tab.name}</Typography>
        <span style={{ visibility: isChanged ? 'visible' : 'hidden' }}>
          <FsDiffIndicator direntId={tab.id} />
        </span>
      </span>
    </Tooltip>
  )
}