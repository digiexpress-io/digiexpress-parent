import { Box } from '@mui/material';
import { Container, Grid, Paper, Typography } from '@mui/material';
import React, { useMemo } from 'react';
import { FormattedMessage, useIntl } from 'react-intl';
import { Bar, BarChart, Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { useConfig } from '../../context/ConfigContext';
import { useFetch } from '../../hooks/useFetch';
import { OverdueByGroupStatistics, TaskPriorityStatistics, TaskStatusStatistics, TaskStatusTimelineStatistics } from '../../types/TaskStatistics';
import { mapRole } from '../../util/rolemapper';
import { TaskPriority, TaskStatus } from '../../types/task/Task';

const chartPaperStyle = {
    flex: '1',
    margin: 1,
    padding: 1,
    borderRadius: 10
};
const chartStyle = {
    flex: '1',
    height: 200,
    marginTop: 2,
};

const FILL_COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042'];

type StatusColorMap = {
  [status in TaskStatus]: string
}
type StatusTitleMap = {
  [status in TaskStatus]: string
}
type PriorityTitleMap = {
  [priority in TaskPriority]: string
}

const statusColorMap: StatusColorMap = {
  NEW: 'brown',
  OPEN: 'blue',
  COMPLETED: 'green',
  REJECTED: 'grey',
};

type PriorityColorMap = {
  [priority in TaskPriority]: string
}

const priorityColorMap: PriorityColorMap = {
  LOW: 'green',
  NORMAL: 'blue',
  HIGH: 'red',
};

const taskStatusMapping:StatusTitleMap = {
  'NEW': 'task.status.new',
  'OPEN': 'task.status.open',
  'REJECTED': 'task.status.rejected',
  'COMPLETED': 'task.status.completed',
}
const taskPriorityMapping:PriorityTitleMap = {
  'LOW': 'task.priority.low',
  'NORMAL': 'task.priority.normal',
  'HIGH': 'task.priority.high',
}

const BarLabel = (props:any) => {
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
  const {tasksApiUrl} = useConfig();
  const {response: taskStatusStats} = useFetch<TaskStatusStatistics[]>(`${tasksApiUrl}/statistics/status`);
  const {response: taskPriorityStats} = useFetch<TaskPriorityStatistics[]>(`${tasksApiUrl}/statistics/priority`);
  const {response: taskTimelineStats} = useFetch<TaskStatusTimelineStatistics[]>(`${tasksApiUrl}/statistics/status-timeline`);
  const {response: overdueStats} = useFetch<OverdueByGroupStatistics[]>(`${tasksApiUrl}/statistics/task-overdue`);

 
  const intl = useIntl();

  const taskStatusNames = useMemo(()=> {
    if (!taskStatusStats) {
      return;
    }
    return taskStatusStats.map(stats=> {
        return {
          status: intl.formatMessage({id: taskStatusMapping[stats.status]}),
          count: stats.count
        }
      });
  }, [intl, taskStatusStats]);

  const taskPriorityNames = useMemo(()=> {
    if (!taskPriorityStats) {
      return;
    }
    return taskPriorityStats.map(stats=> {
        return {
          priority: intl.formatMessage({id: taskPriorityMapping[stats.priority]}),
          count: stats.count
        }
      });
  }, [intl, taskPriorityStats]);

  const tasksOverdue = useMemo(()=> {
    if (!overdueStats) {
      return;
    }
    return overdueStats.map(stats=> {
        return {
          assignedId: mapRole(stats.assignedId),
          count: stats.count
        }
      });
  }, [overdueStats]);


  return (
    <Container maxWidth='lg'>
      <Grid container spacing={2}>
        <Grid item lg={4} sm={6} xs={12}>
          <Paper sx={ chartPaperStyle }>
            <Typography component='h2' variant='h6' gutterBottom>
              <FormattedMessage id='task.statistics.statusCount' />
            </Typography>
            <Box sx={ chartStyle }>
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
        </Grid>
        <Grid item lg={4} sm={6} xs={12}>
          <Paper sx={ chartPaperStyle }>
            <Typography component='h2' variant='h6' gutterBottom>
              <FormattedMessage id='task.statistics.priorityCount' />
            </Typography>
            <Box sx={ chartStyle }>
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
        </Grid>
        <Grid item lg={4} sm={6} xs={12}>
          <Paper sx={ chartPaperStyle }>
            <Typography component='h2' variant='h6' gutterBottom>
              <FormattedMessage id='task.statistics.overdue' />
            </Typography>
            <Box sx={ chartStyle }>
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
        </Grid>
        <Grid item lg={12} sm={12} xs={12}>
          <Paper sx={ chartPaperStyle }>
            <Typography component='h2' variant='h6' gutterBottom>
              <FormattedMessage id='task.statistics.daily' />
            </Typography>
            <Box sx={ chartStyle }>
              <ResponsiveContainer width='95%'>
                <BarChart data={taskTimelineStats} >
                  <XAxis dataKey="statusDate"/>
                  <YAxis allowDecimals={false}/>
                  <Tooltip />
                  <Legend verticalAlign='bottom' />
                  {(['NEW', 'COMPLETED', 'REJECTED'] as TaskStatus[]).map(status=>{
                    return (<Bar dataKey={status.toLowerCase()} 
                      name={intl.formatMessage({id: taskStatusMapping[status]})} 
                      fill={statusColorMap[status]} label={<BarLabel/>}
                      stackId={status === 'COMPLETED' || status === 'REJECTED' ? 'closed' : undefined}/>)
                  })}
                </BarChart>
              </ResponsiveContainer>
            </Box>
          </Paper>
        </Grid>
      </Grid> 
    </Container>
  );
}
