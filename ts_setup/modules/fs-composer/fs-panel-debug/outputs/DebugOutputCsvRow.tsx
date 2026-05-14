import React from 'react';
import { Box, TableCell, TableRow, IconButton, Collapse } from '@mui/material';
import { KeyboardArrowDown as KeyboardArrowDownIcon, KeyboardArrowUp as KeyboardArrowUpIcon } from '@mui/icons-material';
import MonacoReact from '@monaco-editor/react';
import { useIntl } from 'react-intl';
import { toYaml, calcEditorHeight } from './utils';

import { Fs } from '@dxs-ts/fs-api';

const DebugOutputCsvRow: React.FC<{ csvRow: Fs.CsvRow, index: string }> = ({ csvRow, index }) => {
    const intl = useIntl();
    const [open, setOpen] = React.useState(false);
    const yaml = toYaml(csvRow);

    return (<>
      <TableRow sx={{ '& > *': { borderBottom: 'unset' } }}>
        <TableCell>
          <IconButton size="small" onClick={() => setOpen(!open)}>
            {open ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
          </IconButton>
        </TableCell>
        <TableCell component="th" scope="row">
          {intl.formatMessage({ id: 'debug.csv.row' }, { row: index })}
        </TableCell>
      </TableRow>

      <TableRow>
        <TableCell style={{ paddingBottom: 0, paddingTop: 0 }} colSpan={2}>
          <Collapse in={open} timeout="auto" unmountOnExit>
            <Box sx={{ margin: 1 }}>
              <MonacoReact value={yaml} height={calcEditorHeight(yaml)} defaultLanguage='yaml'/>
            </Box>
          </Collapse>
        </TableCell>
      </TableRow>
    </>);
}

export { DebugOutputCsvRow };
