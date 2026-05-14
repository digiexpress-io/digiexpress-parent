import React from 'react';
import { Box, TableCell, TableRow, IconButton, Collapse } from '@mui/material';
import { KeyboardArrowDown as KeyboardArrowDownIcon, KeyboardArrowUp as KeyboardArrowUpIcon  } from '@mui/icons-material';
import { useIntl } from 'react-intl';
import Editor from '@monaco-editor/react';
import { Fs } from '@dxs-ts/fs-api';
import { toYaml } from './utils';



const DebugStep: React.FC<{ debug: Fs.FlowResultLog }> = ({ debug }) => {
  const intl = useIntl();
  const [expanded, setExpanded] = React.useState(false);

  const resp: any = {...debug};
  delete resp['returnsValue']

  const yaml = toYaml(resp);

  return (<>
    <TableRow sx={{ '& > *': { borderBottom: 'unset' } }} key={`${debug.id}-summary`}>
      <TableCell>
        <IconButton size="small" onClick={() => setExpanded(!expanded)}>
          {expanded ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
        </IconButton>
      </TableCell>
      <TableCell component="th" scope="row">
        {intl.formatMessage({ id: 'debug.asset.execute.outputs.flow.step' }, { name: debug.stepId, status: debug.status })}
      </TableCell>
    </TableRow>

    <TableRow key={`${debug.id}-details`} sx={expanded ? undefined : { visibility: "hidden" }}>
      <TableCell style={{ paddingBottom: 0, paddingTop: 0 }} colSpan={2}>
        <Collapse in={expanded} timeout="auto" unmountOnExit>
          <Box sx={{ margin: 1 }}>
            <Editor
              value={yaml}
              onChange={() => {}}
              defaultLanguage='yaml'
              height='500px'
            />
          </Box>
        </Collapse>
      </TableCell>
    </TableRow>
  </>);
}

const DebugOutputsFl: React.FC<{ debug: Fs.FlowResult }> = ({ debug }) => {
  return (<>{debug.logs.map(e => <DebugStep key={`${e.id}-step`} debug={e} />)}</>);
}

export { DebugOutputsFl };
