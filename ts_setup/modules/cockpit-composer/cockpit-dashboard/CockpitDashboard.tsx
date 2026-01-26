import React from 'react';
import { Box, Grid2, Typography } from '@mui/material';
import { CockpitProvider, useCockpit } from '@dxs-ts/cockpit-api';
import {
  CockpitCardConfigContextProvider,
  useCockpitCardConfig,
  cockpitCardGridSize
} from '../cockpit-card';
import { CockpitCardFactory, COCKPIT_CARD_IDS } from '../cockpit-card-factory';
import { CockpitStatusBadge } from './CockpitStatusBadge';

const CockpitDashboardInternal: React.FC = () => {
  const { cockpitContainer, activity } = useCockpit();
  const { cardOrder } = useCockpitCardConfig();

  const isActive = activity.activeCockpitId === cockpitContainer.config.id;
  return (
    <Grid2 container spacing={2} m={1}>
      <Grid2 size={12}>
        <Box display='flex' alignItems='center' justifyContent='space-between' gap={1}>
          <Typography variant='h1'>{cockpitContainer.config.cockpitConfigName}</Typography>
          <CockpitStatusBadge isActive={isActive} />
        </Box>
      </Grid2>

      <Grid2 container size={{ xs: 12 }} spacing={2}>
        {cardOrder.map((cardId) => (
          <Grid2 key={cardId} size={cockpitCardGridSize.singleCol}>
            <CockpitCardFactory cardId={cardId} />
          </Grid2>
        ))}
      </Grid2>
    </Grid2>
  );
};

export const CockpitDashboard: React.FC<{ cockpitId: string }> = (props) => {
  return (
    <CockpitProvider cockpitId={props.cockpitId}>
      <CockpitCardConfigContextProvider initialCardOrder={COCKPIT_CARD_IDS}>
        <CockpitDashboardInternal />
      </CockpitCardConfigContextProvider>
    </CockpitProvider>
  );
};