import React from 'react';
import { Grid2, Typography } from '@mui/material';
import { useIntl } from 'react-intl';

import { CockpitProvider, useCockpit } from '../cockpit-provider';

const CockpitDashboardInternal: React.FC = () => {
  const intl = useIntl();
  const { cockpitContainer } = useCockpit();

  const cardOrder: string[] = [];

  return (
    <Grid2 container spacing={2} m={1}>
      <Grid2 size={12}>
        <Typography variant='h1'>
          {intl.formatMessage({ id: 'cockpit.composer.cockpit.edit' })} 
          {cockpitContainer.config.cockpitConfigName}
        </Typography>
      </Grid2>

      <Grid2 container size={{ xs: 12 }} spacing={2} sx={{ overflowY: 'auto', maxHeight: '100%', overflow: 'visible' }}>
        {cardOrder.map((cardId) => (
          <Grid2 key={cardId} size={12}>
            {/* CockpitCard components will go here */}
            <div>Cockpit Card: {cardId}</div>
          </Grid2>
        ))}
      </Grid2>
    </Grid2>
  );
};

export const CockpitDashboard: React.FC<{ cockpitId: string }> = (props) => {
  return (
    <CockpitProvider cockpitId={props.cockpitId}>
      <CockpitDashboardInternal />
    </CockpitProvider>
  );
};