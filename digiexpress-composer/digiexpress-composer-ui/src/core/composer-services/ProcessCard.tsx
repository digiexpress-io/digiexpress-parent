import React from "react";
import {
  Box, Typography, Card, CardActions, CardContent, Button, Stack
} from "@mui/material";

import ErrorIcon from '@mui/icons-material/Error';
import CheckIcon from '@mui/icons-material/Check';
import { FormattedMessage } from 'react-intl';


import { Client } from '../context';



const ProcessCard: React.FC<{ value: Client.ProcessValue }> = ({ value }) => {
  const ok = true;
  
  return (
    <Card>
      <CardContent>
        <Stack spacing={1}>
          <Box display='flex'>
            <Box flexGrow={1}>
              <Typography color="text.secondary" gutterBottom><FormattedMessage id="composer.services.processName" /></Typography>
              <Typography variant="h4" component="div">{value.name}</Typography>
            </Box>
            <Box>
              {ok ? <CheckIcon color='success'/> : <ErrorIcon color='error'/>}
            </Box>
          </Box>
          <Box>
            <Typography color="text.secondary">
              stencil
            </Typography>
          </Box>
          <Box>
            <Typography color="text.secondary">
              dialob
            </Typography>
          </Box>
          <Box>
            <Typography color="text.secondary">
              wrench
            </Typography>
          </Box>
        </Stack>
      </CardContent>
      <CardActions>
        <Button size="small">Learn More</Button>
      </CardActions>
    </Card>
  );
}


export default ProcessCard;
