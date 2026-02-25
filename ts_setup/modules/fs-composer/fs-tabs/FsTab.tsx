import React from 'react';
import { Tooltip, Typography } from '@mui/material';
import { OwnerState } from './useOwnerState';


export interface FsTabItemProps {
  ownerState: OwnerState;
  className: string;
  index: number;
}


export const FsTab: React.FC<FsTabItemProps> = (props) => {
  const { ownerState, index, className } = props;
  const tab = ownerState.tabs[index];
  return (
    <Tooltip title={tab.name} arrow enterDelay={700} placement="bottom">
      <Typography variant='subtitle2' className={className}>
        {tab.name}
      </Typography>
    </Tooltip>
  )
}