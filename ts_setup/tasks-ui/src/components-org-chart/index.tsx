import React from 'react';
import { ChartTree } from './ChartTree';
import { useRoleChart } from './Role';



const OrgChart: React.FC = () => {
  const { tip } = useRoleChart();
  return (
    <div style={{ overflow: 'auto', paddingTop: '50px'}}>
      <ChartTree value={tip} />
    </div>
  );
}

export default OrgChart;
