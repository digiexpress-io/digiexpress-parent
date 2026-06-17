import React from 'react';
import { Typography } from '@mui/material';
import { FsIcon, FsIcons, useFsTheme } from '../../fs-theme';
import { FsDirentFormFieldProps } from './FsDirentFormFieldProps';
import { useUtilityClasses, FsDirentFormFieldRoot } from './useUtilityClasses';


export const FsDirentFormField: React.FC<FsDirentFormFieldProps> = ({ label, tooltip, helperText, children }) => {
  const { isDarkMode } = useFsTheme();
  const ownerState = { isDarkMode };
  const classes = useUtilityClasses();

  return (
    <FsDirentFormFieldRoot className={classes.root} ownerState={ownerState}>
      <div className={classes.labelRow}>
        <Typography className={classes.label}>{label}</Typography>
        {tooltip && <FsIcon icon={FsIcons.Info} small tooltip={tooltip} />}
      </div>
      {children}
      {helperText && <Typography className={classes.helperText}>{helperText}</Typography>}
    </FsDirentFormFieldRoot>
  );
};
