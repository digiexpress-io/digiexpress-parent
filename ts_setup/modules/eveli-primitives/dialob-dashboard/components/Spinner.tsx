import React from 'react';
import { CircularProgress, Box } from '@mui/material';

export interface SpinnerProps {
  customHeight?: string;
}

export const Spinner: React.FC<SpinnerProps> = ({ customHeight, ...rest }) => {
  return (
    <Box
      minHeight={customHeight || "100px"}
      minWidth="300px"
      alignItems="center"
      display="flex"
      justifyContent="center"
    >
      <CircularProgress color="inherit" {...rest} />
    </Box>
  );
};
