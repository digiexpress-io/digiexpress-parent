import React from 'react';

import { Box, TableCell, TableRow, IconButton, Collapse } from '@mui/material';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';

import { FormattedMessage } from 'react-intl';
import CodeEditor from '../../../code-editor';
import { toYaml } from './utils';
import { HdesApi as Client } from '../../client';

const DebugOutputCsvRow: React.FC<{ csvRow: Client.CsvRow, index: string }> = ({ csvRow, index }) => {
    const [open, setOpen] = React.useState(false);
  
    return (<>
      <TableRow sx={{ '& > *': { borderBottom: 'unset' } }}>
        <TableCell>
          <IconButton size="small" onClick={() => setOpen(!open)}>
            {open ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
          </IconButton>
        </TableCell>
        <TableCell component="th" scope="row">
          <FormattedMessage id="debug.csv.row" values={{row: index}} />
        </TableCell>
      </TableRow>
  
      <TableRow>
        <TableCell style={{ paddingBottom: 0, paddingTop: 0 }} colSpan={2}>
          <Collapse in={open} timeout="auto" unmountOnExit>
            <Box sx={{ margin: 1 }}>
              <CodeEditor id="csv-debug-result" mode="yaml" src={toYaml(csvRow)}
                onChange={(value) => {
                  console.log(value);
                }} />
            </Box>
          </Collapse>
        </TableCell>
      </TableRow>
    </>);
}
  
export { DebugOutputCsvRow };