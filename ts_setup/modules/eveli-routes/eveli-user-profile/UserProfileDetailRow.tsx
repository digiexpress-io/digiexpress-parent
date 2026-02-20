import React from 'react';
import { Chip, Grid2, Typography, useTheme } from '@mui/material';


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


export const UserProfilePermissionsRowChip: React.FC<{ label: React.ReactNode, value: string }> = ({ label, value }) => {
  const theme = useTheme();

  return (
    <Grid2 container>
      <Grid2 size={{ md: 3, lg: 3 }}>
        <Typography fontWeight='bolder' variant='body1'>{label}</Typography>
      </Grid2>
      <Grid2 size={{ md: 9, lg: 9 }}>
        {value.split(",").map((v, index) => <Chip variant='outlined' key={index} label={v}
          sx={{
            borderRadius: 0.5,
            margin: 0.5,
            backgroundColor: theme.palette.secondary.main,
            '& .MuiChip-label': {
              ...theme.typography.caption
            }
          }}
        />)}
      </Grid2>
    </Grid2>
  )
}