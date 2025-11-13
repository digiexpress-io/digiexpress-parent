import React from 'react';
import { Avatar, List, ListItem, ListItemAvatar, ListItemText, Typography } from '@mui/material';
import { Face5 as Face5Icon } from '@mui/icons-material';
import { ContractApi } from '@dxs-ts/contract-api';



export const Policyholder: React.FC<{ contract: ContractApi.ContractSummary }> = ({ contract }) => {
  return (
   <List sx={{ width: '100%', maxWidth: 360, bgcolor: 'background.paper' }} dense disablePadding>
      <ListItem>
        <ListItemAvatar>
          <Avatar  sx={{ width: '32px', height: '32px'}}><Face5Icon /></Avatar>
        </ListItemAvatar>
        <ListItemText primary={contract.policyholder.partyData?.fullName} secondary={contract.policyholder.externalId} />
      </ListItem>
    </List>
  );
};

