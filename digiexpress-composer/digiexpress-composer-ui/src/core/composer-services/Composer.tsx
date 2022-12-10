import React from "react";
import {
  Box, Stack
} from "@mui/material";

import {
} from '@mui/icons-material';
import { FormattedMessage, useIntl } from 'react-intl';


import { Composer, Client } from '../context';
import ComposerMenu from './ComposerMenu';
import ProcessCard from './ProcessCard';




const ComposerServices: React.FC<{ value: Client.ServiceDefinitionDocument }> = ({ value }) => {
  const intl = useIntl();
  const nav = Composer.useNav();
  const service = Composer.useService();
  const [state, setState] = React.useState<Client.SiteDefinition>();
  const { id } = value; 

  React.useEffect(() => {
    service.definition(id).then(setState);
  }, [id]);

  return (
    <Box sx={{ backgroundColor: 'mainContent.main' }}>
      <ComposerMenu value={value}/>
      <Box sx={{ width: '40%', ml: 2, mr: 2 }}>
        <Stack spacing={2}>
          <Box></Box>
          {value.processes.map((proc, key) => <ProcessCard key={key} value={proc} />)}
        </Stack>
      </Box>
    </Box>
  );

}

export default ComposerServices;
