import React from 'react';
import { Typography } from '@mui/material';
import { FsIcon, FsIcons } from '../../fs-theme';
import { FsDirentFormFieldProps } from './FsDirentFormFieldProps';
import { useUtilityClasses, FsDirentFormFieldRoot } from './useUtilityClasses';


export const FsDirentFormField: React.FC<FsDirentFormFieldProps> = ({ label, tooltip, helperText, children }) => {
  const classes = useUtilityClasses();

  return (
    <FsDirentFormFieldRoot className={classes.root}>
      <div className={classes.labelRow}>
        <Typography className={classes.label}>{label}</Typography>
        {tooltip && <FsIcon icon={FsIcons.Info} small tooltip={tooltip} />}
      </div>
      {children}
      {helperText && <Typography className={classes.helperText}>{helperText}</Typography>}
    </FsDirentFormFieldRoot>
  );
};
