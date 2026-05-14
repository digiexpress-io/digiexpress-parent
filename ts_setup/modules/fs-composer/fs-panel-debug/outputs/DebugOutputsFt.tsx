import React from 'react';
import { Box, TableCell, TableRow, IconButton, Collapse } from '@mui/material';
import { KeyboardArrowDown as KeyboardArrowDownIcon, KeyboardArrowUp as KeyboardArrowUpIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl';
import MonacoReact from '@monaco-editor/react';
import { Fs } from '@dxs-ts/fs-api';
import { toYaml, calcEditorHeight } from './utils'


const DebugOutputsFt: React.FC<{ debug: Fs.ServiceResult }> = ({ debug }) => {
  const intl = useIntl();
  const [expanded, setExpanded] = React.useState(false);
  const yaml = toYaml(debug.value);

  return (<>
    <TableRow sx={{ '& > *': { borderBottom: 'unset' } }}>
      <TableCell>
        <IconButton size="small" onClick={() => setExpanded(!expanded)}>
          {expanded ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
        </IconButton>
      </TableCell>
      <TableCell component="th" scope="row">
        {intl.formatMessage({ id: 'debug.asset.execute.outputs.service' })}
      </TableCell>
    </TableRow>

    <TableRow sx={expanded ? undefined : { visibility: "hidden" }}>
      <TableCell style={{ paddingBottom: 0, paddingTop: 0 }} colSpan={2}>
        <Collapse in={expanded} timeout="auto" unmountOnExit>
          <Box sx={{ margin: 1 }}>
            <MonacoReact key="debug-output"
              value={yaml}
              defaultLanguage='yaml'
              height={calcEditorHeight(yaml)}
              />
          </Box>
        </Collapse>
      </TableCell>
    </TableRow>
  </>);
}

export { DebugOutputsFt };
