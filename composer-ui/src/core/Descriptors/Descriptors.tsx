import React from "react";
import {
  Box, Stack, CircularProgress
} from "@mui/material";

import {
} from '@mui/icons-material';
import { FormattedMessage, useIntl } from 'react-intl';


import DeClient from '@declient';
import ComposerMenu from './ComposerMenu';
import DescriptorTable from '../DescriptorTable';


const ComposerServices: React.FC<{}> = ({ }) => {
  const intl = useIntl();
  const nav = DeClient.useNav();
  const service = DeClient.useService();
  const session = DeClient.useSession();
  const [state, setState] = React.useState<DeClient.DefinitionState>();
  const def = Object.values(session.head.definitions).find(() => true);

  React.useEffect(() => {
    if (def) {
      service.definition(def.id).then(setState);
    }
  }, [def]);

  if (!state || !def) {
    return (<Box sx={{ display: 'flex' }}><CircularProgress /></Box>);
  }

  return (<Box sx={{ backgroundColor: 'mainContent.main' }}>
    <ComposerMenu value={def} />
    <Box><DescriptorTable def={state} /></Box>
  </Box>);

}

export default ComposerServices;
