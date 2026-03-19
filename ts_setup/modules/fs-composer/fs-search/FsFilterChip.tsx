import React from 'react';
import { Chip, styled, generateUtilityClass } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsDirentType, getDirentColor } from '../fs-theme';

const MUI_NAME = 'FsFilterChip';


export interface FsFilterChipClasses {
  root: string;
}

interface FsFilterChipProps {
  label: string;
  chipType: FsDirentType;
  isDarkMode: boolean;
}


const useUtilityClasses = (_props: FsFilterChipProps) => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

const FsFilterChipRoot = styled(Chip, {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'chipType' && prop !== 'isDarkMode',
})<{ chipType: FsDirentType; isDarkMode: boolean }>(({ theme, chipType, isDarkMode }) => {
  const baseColor = getDirentColor(chipType, isDarkMode);
  return {
    backgroundColor: baseColor + '20',
    borderColor: baseColor,
    border: `1px solid ${baseColor}`,
    color: baseColor,
    fontWeight: 'bold',
    ...theme.typography.caption,
    '&:hover': {
      backgroundColor: baseColor + '50',
      borderColor: baseColor,
    },
    '& .MuiChip-deleteIcon': {
      color: baseColor,
      '&:hover': {
        color: baseColor,
      }
    }
  };
});

export const FsFilterChip: React.FC<FsFilterChipProps> = (props) => {
  const classes = useUtilityClasses(props);

  return (
    <FsFilterChipRoot
      chipType={props.chipType}
      isDarkMode={props.isDarkMode}
      className={classes.root}
      label={props.label}
      size="small"
    />
  );
};