import { Box } from '@mui/material';
import { Grid2, Paper, Typography } from '@mui/material';
import React from 'react';
import { Legend, PieChart, ResponsiveContainer, Tooltip } from 'recharts';

import { TaskApi } from '@dxs-ts/eveli-api';


const chartPaperStyle = {
  flex: '1',
  marginTop: 2,
  padding: 1,
  borderRadius: 1
};
const chartStyle = {
  flex: '1',
  height: 300,
  marginTop: 2,
};


type StatusColorMap = {
  [status in TaskApi.TaskStatus]: string
}


type PriorityColorMap = {
  [priority in TaskApi.TaskPriority]: string
}

export const OVERDUE_FILL_COLORS = ['#1976D2', '#388E3C', '#FB8C00', '#D32F2F'];

export const priorityColorMap: PriorityColorMap = {
  LOW: '#388E3C',
  NORMAL: '#1976D2',
  HIGH: '#D32F2F',
};

export const statusColorMap: StatusColorMap = {
  NEW: '#FB8C00',
  OPEN: '#388E3C',
  COMPLETED: '#1976D2',
  REJECTED: '#D32F2F',
  TRANSFERRED: 'grey',
  DELEGATED: 'grey',
  WAITING: 'yellow'

};

export const BarLabel = (props: any) => {
  const { value, x, y, width } = props;

  if (value > 0) {
    return (
      <text
        x={(x + width / 2) - 5}
        y={y + 20}
        style={{
          fontSize: "12pt",
          fontWeight: "bold",

        }}

      >
        {value}
      </text>
    );
  } else {
    return <text></text>;
  }
};



export const BarChartSlot: React.FC<{ label: React.ReactNode, bar: React.ReactElement }> = (props) => {
  return (
    <Grid2 size={{ xs: 12, sm: 12, lg: 12 }}>
      <Paper sx={chartPaperStyle}>
        <Typography component='h2' fontWeight='bold' gutterBottom>
          {props.label}
        </Typography>
        <Box sx={chartStyle}>
          <ResponsiveContainer width='95%'>
            {props.bar}
          </ResponsiveContainer>
        </Box>
      </Paper>
    </Grid2>
  )
}

export const PieChartSlot: React.FC<{ label: React.ReactNode, pie: React.ReactNode, fullwidth?: boolean }> = (props) => {
  return (
    <Grid2 size={{ xs: 12, sm: props.fullwidth ? 12 : 6, lg: props.fullwidth ? 12 : 4 }}>
      <Paper sx={chartPaperStyle}>
        <Typography component='h2' fontWeight='bold' gutterBottom>
          {props.label}
        </Typography>
        <Box sx={chartStyle}>
          <ResponsiveContainer width='95%'>
            <PieChart>
              {props.pie}
              <Legend verticalAlign='bottom' />
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </Box>
      </Paper>
    </Grid2>
  )
}