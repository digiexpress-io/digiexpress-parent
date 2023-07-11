import React from "react";
import {
  Box, Stack, CircularProgress
} from "@mui/material";

import {
} from '@mui/icons-material';
import { FormattedMessage, useIntl } from 'react-intl';


import DeClient from '@declient';

import DiagramCanvas from './DiagramCanvas';

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

const ProjectDiagram: React.FC<{}> = ({ }) => {

  const { head } = DeClient.useSession();
  const defs = Object.values(head.definitions);
  const intl = useIntl();
  const nav = DeClient.useNav();
  const service = DeClient.useService();
  const [state, setState] = React.useState<DeClient.DefinitionState>();

  const def = defs.find(() => true);

  React.useEffect(() => {
    if (def) {
      service.definition(def.id).then(setState);
    }
  }, [def]);

  const result = React.useMemo(() => {
    if (!state) {
      return null;
    }
    return <DiagramCanvas site={state} />;
  }, [state])


  if (defs.length === 0) {
    return <>no project</>;
  }
  if (!state) {
    return (<Box sx={{ display: 'flex' }}><CircularProgress /></Box>);
  }
  return (<Box sx={{ height: '9000px' }}>{result}</Box>);

}

export default ProjectDiagram;
