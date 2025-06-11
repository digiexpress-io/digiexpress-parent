import React from 'react';
import { Grid2, Typography, useTheme } from '@mui/material';
import CircleIcon from '@mui/icons-material/Circle';

interface ActivityItem {
  taskName: string;
  updated: string;
  assignee: string;
  comments: string;
}

const data: ActivityItem[] = [
  { taskName: 'General message', updated: '2025-06-01', assignee: 'Me', comments: 'ASAP processing' },
  { taskName: 'School counselor', updated: '2025-06-02', assignee: 'Bob', comments: '' },
  { taskName: 'Building permit', updated: '2025-06-03', assignee: 'Me', comments: 'Request additional info from Charlie' },
  { taskName: 'General Message', updated: '2025-06-04', assignee: 'Dana', comments: '' },
];


export const UserActivity: React.FC = () => {
  const theme = useTheme();


  return (
    <>
      <Headers />
      {data.map((item, index) => (
        <Grid2 container spacing={1} alignItems="center" key={index}
          sx={{
            backgroundColor: index % 2 === 0 ? 'transparent' : theme.palette.secondary.main,
            padding: 1,
            ':hover': {
              backgroundColor: theme.palette.secondary.dark,
              cursor: 'pointer'
            }
          }}>
          <Grid2 size={{ md: 4, lg: 4, xl: 4 }} sx={{ display: 'flex', alignItems: 'center' }}>
            <CircleIcon sx={{ fontSize: '5pt', mr: theme.spacing(1), color: theme.palette.primary.main }} />
            <Typography variant='body1'>{item.taskName}</Typography>
          </Grid2>

          <Grid2 size={{ md: 2, lg: 2, xl: 2 }}>
            <Typography variant='body1'>{item.updated}</Typography>
          </Grid2>

          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography variant='body1'>{item.assignee}</Typography>
          </Grid2>

          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography variant='body1'>{item.comments}</Typography>
          </Grid2>
        </Grid2>
      ))}
    </>)
}

const Headers: React.FC = () => {
  return (
    <Grid2 container spacing={1} padding={1} sx={{ fontWeight: 'bold' }}>
      <Grid2 size={{ md: 4, lg: 4, xl: 4 }}>Task name</Grid2>
      <Grid2 size={{ md: 2, lg: 2, xl: 2 }}>Updated</Grid2>
      <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>Assignee</Grid2>
      <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>Comments</Grid2>
    </Grid2>
  )
}