import React from 'react';
import { Box, Grid, Stack, Typography, Table, TableCell, TableRow, TableContainer, Paper, alpha } from '@mui/material';
import CircleIcon from '@mui/icons-material/Circle';
import { PieChart, Pie, Cell } from 'recharts';
import { FormattedMessage } from 'react-intl';

import SampleReport from './reporting-sample';
import { createReport } from './reporting-types';


const MyWork: React.FC<{}> = () => {

  const data = createReport(SampleReport());

  return (
    <>
      <Typography variant='h3'><FormattedMessage id='core.myOverview.title' /></Typography>
      <Grid container>
        <Grid item lg={4} md={4} sm={12} xs={12}>
          <PieChart width={300} height={300}>
            <Pie data={data.groups} dataKey="value" cx="50%" cy="50%" outerRadius={60} fill="#82ca9d">
              {data.groups.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={entry.color} />
              ))}
            </Pie>
            <Pie data={data.events}
              dataKey="value" cx="50%" cy="50%"
              innerRadius={70} outerRadius={100} fill="#82ca9d" label>
              {data.events.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={entry.color} />
              ))}
            </Pie>
          </PieChart>
        </Grid>
        <Grid item lg={8} md={8} sm={12} xs={12} alignSelf='center'>
          <Stack spacing={1}>
            {data.groups.flatMap(group => group.events).map(item => (
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <CircleIcon sx={{ color: item.color }} />
                <Typography variant='body1'>
                  <FormattedMessage id={`core.myOverview.${item.type}`} />
                </Typography>
              </Box>
            ))}
          </Stack>
        </Grid>
      </Grid>
      <Paper sx={{ width: 0.6 }}>
        <TableContainer>
          <Table>
            {data.groups.map((group, index) => (
              <TableRow key={`group-row-${index}`}>
                <TableCell variant="head" sx={{ backgroundColor: alpha(group.color, 0.5) }}>
                  <Typography variant='h5'><FormattedMessage id={`core.myOverview.${group.name}`} /></Typography>
                </TableCell>
                <TableCell>
                  {group.events.map((event, index) => (
                    <TableRow key={`event-${index}`} >
                      <Typography>
                        <FormattedMessage id={`core.myOverview.${event.type}`} />
                      </Typography>
                    </TableRow>
                  ))}
                </TableCell>
                <TableCell>
                  {group.events.map((event, index) => (
                    <TableRow key={`value-${index}`} >
                      <Typography>
                        {event.value}
                      </Typography>
                    </TableRow>
                  ))}
                </TableCell>
              </TableRow>
            ))}
          </Table>
        </TableContainer>
      </Paper>
    </>
  );
}

export default MyWork;
