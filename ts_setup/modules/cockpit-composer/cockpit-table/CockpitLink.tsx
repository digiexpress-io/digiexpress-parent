import React from 'react';
import { Box, Link } from '@mui/material';

import { useCockpitsBackend } from '@dxs-ts/cockpit-api';


export type CockpitLinkProps = {
  name: string
  id: string
}


export const CockpitLink: React.FC<CockpitLinkProps> = ({ name, id }) => {
  const backend = useCockpitsBackend();

  return (
    <Box
      sx={{
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
        minWidth: 0,
      }}
    >
      <Link href="#" onClick={(event) => {
        event.stopPropagation();
        event.preventDefault();
        // TODO: Replace with actual navigation method when available
        // backend.navigate.openOneCockpit(id);
        console.log('Navigate to cockpit:', id);
      }}>
        {name}
      </Link>
    </Box>
  );
};