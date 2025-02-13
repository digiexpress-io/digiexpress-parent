import React from 'react';
import { ChartTree } from './ChartTree';
import { useRoleChart } from './Role';
import { Paper } from '@mui/material';



const OrgChart: React.FC = () => {
  const { tip } = useRoleChart();
  return (
    <Paper sx={{ p: 1, height: '100%' }}>
      <div style={{ overflow: 'auto', paddingTop: '50px' }}>
        <ChartTree value={tip} />
      </div>
    </Paper>
  );
}

export default OrgChart;
