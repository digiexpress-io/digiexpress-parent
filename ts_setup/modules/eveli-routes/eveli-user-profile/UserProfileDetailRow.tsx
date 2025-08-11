import React from 'react';
import { Grid2, Typography } from '@mui/material';


export const UserProfileDetailRow: React.FC<{ label: React.ReactNode, value: any }> = ({ label, value }) => {

  return (
    <Grid2 container>
      <Grid2 size={{ md: 3, lg: 3 }}>
        <Typography fontWeight='bolder' variant='body1'>{label}</Typography>
      </Grid2>
      <Grid2 size={{ md: 9, lg: 9 }} textAlign='left'>
        <Typography variant='subtitle2'>{value}</Typography>
      </Grid2>
    </Grid2>
  )
}