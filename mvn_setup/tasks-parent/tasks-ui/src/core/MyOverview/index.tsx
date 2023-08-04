import React from 'react';
import { Box, Grid, Stack } from '@mui/material';
import CircleIcon from '@mui/icons-material/Circle';
import { PieChart, Pie, Cell} from 'recharts';

import SampleReport from './reporting-sample';
import { createReport } from './reporting-types';



const MyWork: React.FC<{}> = () => {

  const data = createReport(SampleReport());

  console.log(data);

  return (
    <Grid container>
      <Grid item lg={6}>
        <PieChart width={250} height={250}>
          <Pie data={data.groups} dataKey="value" cx="50%" cy="50%" outerRadius={60} fill="#82ca9d">
          </Pie>
          <Pie data={data.events}
            dataKey="value" cx="50%" cy="50%"
            innerRadius={70} outerRadius={90} fill="#82ca9d">

            {data.events.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={entry.color} />
            ))}
          </Pie>

        </PieChart>
      </Grid>
      <Grid item lg={6}>
        <Stack spacing={1}>
          {data.groups.flatMap(group => group.events).map(item => (
            <Box><CircleIcon sx={{ color: item.color }} />{item.type} {item.value}</Box>
          ))}
        </Stack>

      </Grid>
    </Grid>
  );
}


export default MyWork;