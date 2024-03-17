import React from 'react';
import { ChartProps } from './org-chart-types'
import { ChartTreeFolder } from './ChartTreeFolder';
import { ChartTreeTable } from './ChartTreeTable';



const OrgChart: React.FC = () => {
  return (
    <div style={{ overflow: 'auto' }}>
      <div>Neat folder like view</div>
      <ChartTreeFolder value={testData} />
      <div>Neat tree like view</div>
      <ChartTreeTable value={testData} />
    </div>
  );
}

export const testData: ChartProps = {
  label: 'Role 1',
  expanded: true,
  children: [
    {
      label: 'Role 2',
      expanded: true,
      children: [
        {
          label: 'Amanda',
        },
        {
          label: 'Jim',
        },
        {
          label: 'Jenny',
        },
        {
          label: 'Hank',
        },
      ],
    },
    {
      label: 'Role 4',
      expanded: true,
      children: [
        {
          label: 'John',
          expanded: true,
        },
        {
          label: 'Amy',
          expanded: true,
        },
        {
          label: 'Jill',
        },
        {
          label: 'Mark',
        },
        {
          label: 'Role 5',
          expanded: true,
          children: [
            {
              label: 'Henry',
            },
            {
              label: 'Julia',
            },
            {
              label: 'Thomas Super long name',
            },
          ],
        },
      ],
    },
    {
      label: 'Role 3',
      expanded: true,
      children: [
        {
          label: 'Frank',
        },
        {
          label: 'Joseph',
        },
      ],
    },
  ],
}

export default OrgChart;
