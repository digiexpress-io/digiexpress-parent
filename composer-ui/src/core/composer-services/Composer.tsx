import React from "react";
import {
  Box, Stack
} from "@mui/material";

import {
} from '@mui/icons-material';
import { FormattedMessage, useIntl } from 'react-intl';


import DeClient from '@declient';
import ComposerMenu from './ComposerMenu';
import ProcessCard from './ProcessCard';

import Diagram from './Diagram';

/**

    <Box sx={{ backgroundColor: 'mainContent.main' }}>
      <ComposerMenu value={value}/>
      <Box sx={{ width: '40%', ml: 2, mr: 2 }}>
        <Stack spacing={2}>
          <Box></Box>
          {value.processes.map((proc, key) => <ProcessCard key={key} value={proc} />)}
        </Stack>
      </Box>
    </Box>

 */

const ComposerServices: React.FC<{ value: DeClient.ServiceDefinition }> = ({ value }) => {
  const intl = useIntl();
  const nav = DeClient.useNav();
  const service = DeClient.useService();
  const [state, setState] = React.useState<DeClient.DefinitionState>();
  const { id } = value; 

  React.useEffect(() => {
    service.definition(id).then(setState);
  }, [id]);


  const result = React.useMemo(() => {
    if(!state) {
      return null;
    }
    
    return <Diagram site={state}/>;
  }, [state]) 
  return (<Box sx={{ height: '9000px' }}>{result}</Box>);

}

export default ComposerServices;
