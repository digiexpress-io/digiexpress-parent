import React from 'react';
import { Accordion, AccordionSummary, AccordionDetails, Typography, useTheme } from '@material-ui/core';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import { FormattedMessage } from 'react-intl';

import { CodeEditor } from '../deps';
import { useContext, Session } from './context';

const DebugOutputsFt: React.FC<{}> = () => {
  
  const context = useContext();
  const theme = useTheme();
  const active: Session.Active = context.active as Session.Active;
  
  return (
    <Accordion expanded={context.session.outputs} onChange={() => context.actions.handleOutputs(!context.session.outputs)}>
      <AccordionSummary expandIcon={<ExpandMoreIcon />}>
        <Typography color="primary"><FormattedMessage id={"debug.asset.execute.outputs"} /></Typography>
      </AccordionSummary>
      <AccordionDetails>
        <CodeEditor.Provider mode="json" theme={theme.palette.mode === 'dark' ? 'dark' : 'light'} onCommands={() => { }}>
          {JSON.stringify(active.debug.output.service, null, 2)}
        </CodeEditor.Provider>
      </AccordionDetails>
    </Accordion>
  );
}

export { DebugOutputsFt };


