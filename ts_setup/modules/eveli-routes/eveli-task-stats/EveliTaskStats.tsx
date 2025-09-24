import { Box } from '@mui/material';
import { Container, Grid2, Paper, Typography } from '@mui/material';
import React from 'react';
import { FormattedMessage, useIntl } from 'react-intl';
import { Bar, BarChart, Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';

import { useFetch } from '@dxs-ts/envir-fetch';
import { TaskApi } from '@dxs-ts/task-api';

import { OVERDUE_FILL_COLORS, PieChartSlot, priorityColorMap, statusColorMap, BarChartSlot, BarLabel } from './useUtilityClasses';
import { withDs } from './WithDashboardData';



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
  const intl = useIntl();

  function getStatusName(item: TaskApi.GrimMissionAttributeEvent): string {
    return intl.formatMessage({id: `task.status.${item.attributeValue.toLocaleLowerCase()}`});
  }
  function getPriorityName(item: TaskApi.GrimMissionAttributeEvent): string {
    return intl.formatMessage({id: `task.priority.${item.attributeValue.toLocaleLowerCase()}`});
  }


  return (
    <Container maxWidth='lg'>
      <Grid2 container spacing={2}>
        
        <PieChartSlot 
          label={<FormattedMessage id='task.statistics.statusCount' />}
          pie={ withDs(dashoard)
            .intl(getStatusName)
            .fill(item => statusColorMap[item.attributeValue as TaskApi.TaskStatus])
            .filter(item => item.eventType === 'STATUS')
            .map(data => <Pie label dataKey="eventCount" nameKey="intl" data={data} >
                {data.map((entry, index) => (<Cell key={`cell-${index}`} fill={entry.fill} />))}
              </Pie>)
          }
        />

        <PieChartSlot 
          label={<FormattedMessage id='task.statistics.priorityCount' />}
          pie={withDs(dashoard)
            .intl(getPriorityName)
            .fill(item => priorityColorMap[item.attributeValue as TaskApi.TaskPriority])
            .filter(item => item.eventType === 'PRIORITY')
            .map(data => <Pie label dataKey="eventCount" nameKey="intl" data={data} >
                {data.map((entry, index) => (<Cell key={`cell-${index}`} fill={entry.fill} />))}
              </Pie>)
          }
        />

        <PieChartSlot 
          label={<FormattedMessage id='task.statistics.overdue' />}
          pie={withDs(dashoard)
            .filter(item => item.eventType === 'OVERDUE')
            .map(data => <Pie label dataKey="eventCount" nameKey="intl" data={data} >
                {data.map((entry, index) => (<Cell key={`cell-${index}`} fill={entry.fill} />))}
              </Pie>)
          }
        />


        <PieChartSlot
          label={<FormattedMessage id='task.statistics.new_task_by_role' />}
          pie={withDs(dashoard)
            .filter(item => item.eventType === 'ROLE' && item.eventSubType === 'NEW')
            .map(data => <Pie dataKey="eventCount" nameKey="intl" data={data} legendType='none'>
                {data.map((entry, index) => (<Cell key={`cell-${index}`} fill={entry.fill} />))}
              </Pie>)
          }
        />
        <PieChartSlot
          label={<FormattedMessage id='task.statistics.open_task_by_role' />}
          pie={withDs(dashoard)
            .filter(item => item.eventType === 'ROLE' && item.eventSubType === 'OPEN')
            .map(data => <Pie dataKey="eventCount" nameKey="intl" data={data} legendType='none'>
                {data.map((entry, index) => (<Cell key={`cell-${index}`} fill={entry.fill} />))}
              </Pie>)}
        />

        <PieChartSlot
          label={<FormattedMessage id='task.statistics.task_by_questionnaire' />}
          pie={withDs(dashoard)
            .filter(item => item.eventType === 'QUESTIONNAIRE')
            .map(data => <Pie dataKey="eventCount" nameKey="intl" data={data} legendType='none'>
                {data.map((entry, index) => (<Cell key={`cell-${index}`} fill={entry.fill} />))}
              </Pie>)
          }
        />

        <BarChartSlot 
          label={<FormattedMessage id='task.statistics.daily' />}
          bar={
            withDs(dashoard)
              .filter(item => item.eventType === 'STATUS_DATE')
              .groupByDate(data => <BarChart data={data.filter(({ eventAgeInMonths }) => eventAgeInMonths < 6) } >
                <XAxis dataKey="eventDate" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Legend verticalAlign='bottom' />
                {(['NEW', 'COMPLETED', 'REJECTED'] as TaskApi.TaskStatus[]).map((status, index) => 
                   (<Bar key={index} dataKey={status.toLowerCase()}
                    name={getStatusName({ attributeValue: status } as any)}
                    fill={statusColorMap[status]} label={<BarLabel />}
                    stackId={status === 'COMPLETED' || status === 'REJECTED' ? 'closed' : undefined} />)
                )}
              </BarChart>)
           }
        />

      </Grid2>
    </Container>
  );
}
