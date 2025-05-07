import { Box } from '@mui/material';
import { Container, Grid2, Paper, Typography } from '@mui/material';
import React from 'react';
import { FormattedMessage, useIntl } from 'react-intl';
import { Bar, BarChart, Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';


import { useFetch } from '@dxs-ts/eveli-fetch';
import { TaskApi } from '../api-task';
import { useTaskOverdue } from './useTaskOverdue';
import { useStatusTimeline } from './useStatusTimeline';
import { usePriorityCount } from './usePriorityCount';
import { useStatusCount } from './useStatusCount';

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

const OVERDUE_FILL_COLORS = ['#1976D2', '#388E3C', '#FB8C00', '#D32F2F'];

type StatusColorMap = {
  [status in TaskApi.TaskStatus]: string
}


const statusColorMap: StatusColorMap = {
  NEW: '#FB8C00',
  OPEN: '#388E3C',
  COMPLETED: '#1976D2',
  REJECTED: '#D32F2F',
  TRANSFERRED: 'grey',

};

type PriorityColorMap = {
  [priority in TaskApi.TaskPriority]: string
}

const priorityColorMap: PriorityColorMap = {
  LOW: '#388E3C',
  NORMAL: '#1976D2',
  HIGH: '#D32F2F',
};


const BarLabel = (props: any) => {
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



export const EveliTaskStats: React.FC = () => {
  const taskFetch = useFetch('worker/rest/api/tasks.GET', {})
  const [dashboard, setDashboard] = React.useState<TaskApi.TaskDasboard>();

  React.useEffect(() => {
    taskFetch.dashboard().then(setDashboard)
  }, []);

  if(!dashboard) {
    return (<>...loading</>)
  }
  return (<EveliTaskBody dashoard={dashboard}/>)
}


const EveliTaskBody: React.FC<{ dashoard: TaskApi.TaskDasboard }> = ({dashoard}) => {

  const { taskStatusNames, taskStatusMapping, taskStatusStats } = useStatusCount(dashoard);
  const { taskPriorityNames, taskPriorityStats } = usePriorityCount(dashoard);
  const { taskTimelineStats } = useStatusTimeline(dashoard);
  const { tasksOverdue } = useTaskOverdue(dashoard);
  const intl = useIntl();

  return (
    <Container maxWidth='lg'>
      <Grid2 container spacing={2}>
        <Grid2 size={{ xs: 12, sm: 6, lg: 4 }}>
          <Paper sx={chartPaperStyle}>
            <Typography component='h2' fontWeight='bold' gutterBottom>
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
            <Typography component='h2' fontWeight='bold' gutterBottom>
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
            <Typography component='h2' fontWeight='bold' gutterBottom>
              <FormattedMessage id='task.statistics.overdue' />
            </Typography>
            <Box sx={chartStyle}>
              <ResponsiveContainer width='95%'>
                <PieChart>
                  <Pie data={tasksOverdue} dataKey="count" nameKey="assignedId" label>
                    {tasksOverdue?.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={OVERDUE_FILL_COLORS[index % OVERDUE_FILL_COLORS.length]} />
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
            <Typography component='h2' fontWeight='bold' gutterBottom>
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
