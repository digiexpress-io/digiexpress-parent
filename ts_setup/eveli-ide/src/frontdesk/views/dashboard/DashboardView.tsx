import { Box } from '@mui/material';
import { Container, Grid2, Paper, Typography } from '@mui/material';
import React from 'react';
import { FormattedMessage, useIntl } from 'react-intl';
import { Bar, BarChart, Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';


import { useFetch } from '@dxs-ts/eveli-fetch';
import { TaskApi } from '@/burger';

const chartPaperStyle = {
  flex: '1',
  marginTop: 2,
  padding: 1,
  borderRadius: 2
};
const chartStyle = {
  flex: '1',
  height: 200,
  marginTop: 2,
};

const FILL_COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042'];

type StatusColorMap = {
  [status in TaskApi.TaskStatus]: string
}


const statusColorMap: StatusColorMap = {
  NEW: 'brown',
  OPEN: 'blue',
  COMPLETED: 'green',
  REJECTED: 'grey',
};

type PriorityColorMap = {
  [priority in TaskApi.TaskPriority]: string
}

const priorityColorMap: PriorityColorMap = {
  LOW: 'green',
  NORMAL: 'blue',
  HIGH: 'red',
};




const BarLabel = (props: any) => {
  const {
    value,
    ...rest
  } = props;

  if (value > 0) {
    return (
      <text
        {...rest}
        className="recharts-bar-label">
        {value}
      </text>
    );
  } else {
    return <text></text>;
  }
};

export const DashboardView: React.FC = () => {
  const { taskStatusNames, taskStatusMapping, taskStatusStats } = useFetch('statistics/status.GET', {});
  const { taskPriorityNames, taskPriorityStats } = useFetch('statistics/priority.GET', {});
  const { taskTimelineStats } = useFetch('statistics/status-timeline.GET', {});
  const { tasksOverdue } = useFetch('statistics/task-overdue.GET', {});
  const intl = useIntl();

  return (
    <Container maxWidth='lg'>
      <Grid2 container spacing={2}>
        <Grid2 size={{ xs: 12, sm: 6, lg: 4 }}>
          <Paper sx={chartPaperStyle}>
            <Typography component='h2' variant='h6' gutterBottom>
              <FormattedMessage id='task.statistics.statusCount' />
            </Typography>
            <Box sx={chartStyle}>
              <ResponsiveContainer width='95%'>
                <PieChart>
                  <Pie data={taskStatusNames} dataKey="count" nameKey="status" label>
                    {taskStatusStats?.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={statusColorMap[entry.status]} />
                    ))}
                  </Pie>
                  <Legend verticalAlign='bottom' />
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </Box>
          </Paper>
        </Grid2>
        <Grid2 size={{ xs: 12, sm: 6, lg: 4 }}>
          <Paper sx={chartPaperStyle}>
            <Typography component='h2' variant='h6' gutterBottom>
              <FormattedMessage id='task.statistics.priorityCount' />
            </Typography>
            <Box sx={chartStyle}>
              <ResponsiveContainer width='95%'>
                <PieChart>
                  <Pie data={taskPriorityNames} dataKey="count" nameKey="priority" label>
                    {taskPriorityStats?.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={priorityColorMap[entry.priority]} />
                    ))}
                  </Pie>
                  <Legend verticalAlign='bottom' />
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </Box>
          </Paper>
        </Grid2>
        <Grid2 size={{ xs: 12, sm: 6, lg: 4 }}>
          <Paper sx={chartPaperStyle}>
            <Typography component='h2' variant='h6' gutterBottom>
              <FormattedMessage id='task.statistics.overdue' />
            </Typography>
            <Box sx={chartStyle}>
              <ResponsiveContainer width='95%'>
                <PieChart>
                  <Pie data={tasksOverdue} dataKey="count" nameKey="assignedId" label>
                    {tasksOverdue?.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={FILL_COLORS[index % FILL_COLORS.length]} />
                    ))}
                  </Pie>
                  <Legend verticalAlign='bottom' />
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </Box>
          </Paper>
        </Grid2>
        <Grid2 size={{ xs: 12, sm: 12, lg: 12 }}>
          <Paper sx={chartPaperStyle}>
            <Typography component='h2' variant='h6' gutterBottom>
              <FormattedMessage id='task.statistics.daily' />
            </Typography>
            <Box sx={chartStyle}>
              <ResponsiveContainer width='95%'>
                <BarChart data={taskTimelineStats} >
                  <XAxis dataKey="statusDate" />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Legend verticalAlign='bottom' />
                  {(['NEW', 'COMPLETED', 'REJECTED'] as TaskApi.TaskStatus[]).map((status, index) => {
                    return (<Bar key={index} dataKey={status.toLowerCase()}
                      name={intl.formatMessage({ id: taskStatusMapping[status] })}
                      fill={statusColorMap[status]} label={<BarLabel />}
                      stackId={status === 'COMPLETED' || status === 'REJECTED' ? 'closed' : undefined} />)
                  })}
                </BarChart>
              </ResponsiveContainer>
            </Box>
          </Paper>
        </Grid2>
      </Grid2>
    </Container>
  );
}
