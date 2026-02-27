import React from 'react';
import { Chip, styled, generateUtilityClass } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsNodeType, getNodeColor } from '../fs-theme';

const MUI_NAME = 'FsFilterChip';

interface FsFilterChipProps {
  label: string;
  chipType: FsNodeType;
  isDarkMode: boolean;
  size: 'small' | 'medium';
}

interface FsFilterChipClasses {
  root: string;
}

interface OwnerState {
  chipType: FsNodeType;
  isDarkMode: boolean;
  size: 'small' | 'medium';
  baseColor: string;
}

const useUtilityClasses = (props: FsFilterChipProps) => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

const useOwnerState = (props: FsFilterChipProps): OwnerState => {
  const baseColor = getNodeColor(props.chipType, props.isDarkMode);

  return {
    chipType: props.chipType,
    isDarkMode: props.isDarkMode,
    size: props.size,
    baseColor,
  };
};

const FsFilterChipRoot = styled(Chip, {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({
  backgroundColor: ownerState.baseColor + '20',
  borderColor: ownerState.baseColor,
  border: `1px solid ${ownerState.baseColor}`,
  color: ownerState.baseColor,
  fontWeight: 'bold',
  ...theme.typography.caption,
  '&:hover': {
    backgroundColor: ownerState.baseColor + '50',
    borderColor: ownerState.baseColor,
  },
  '& .MuiChip-deleteIcon': {
    color: ownerState.baseColor,
    '&:hover': {
      color: ownerState.baseColor,
    }
  }
}));

export const FsFilterChip: React.FC<FsFilterChipProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses(props);

  return (
    <FsFilterChipRoot
      ownerState={ownerState}
      className={classes.root}
      label={props.label}
      size={props.size}
    />
  );
};