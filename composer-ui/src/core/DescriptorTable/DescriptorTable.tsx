import React from 'react';


import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TablePagination from '@mui/material/TablePagination';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';


import DeClient from '@declient';

import { DescriptorPagination } from './descriptor-pagination';
import { Provider } from './descriptor-table-ctx';
import DescriptorTableHeader from './DescriptorTableHeader';
import DescriptorTableRow from './DescriptorTableRow';



const DescriptorTable: React.FC<{ def: DeClient.DefinitionState }> = ({ def }) => {
  const [content, setContent] = React.useState(new DescriptorPagination({ def: def.definition }));
  const assocs = React.useMemo(() => new DeClient.DefStateAssocsImpl({ def }), [def]);

  return (<Provider>
    <Paper sx={{ width: '100%', mb: 2 }}>
      <TableContainer>
        <Table size='small'>
          <TableHead><DescriptorTableHeader content={content} setContent={setContent} /></TableHead>
          <TableBody>
            {content.entries.map((row) => (<DescriptorTableRow key={row.id} row={row} assocs={assocs} def={def} />))}
            {content.emptyRows > 0 ? <TableRow style={{ height: (28 + 1) * content.emptyRows }}><TableCell colSpan={6} /></TableRow> : null}
          </TableBody>
        </Table>
      </TableContainer>
      <TablePagination
        rowsPerPageOptions={content.rowsPerPageOptions}
        component="div"
        count={def.definition.descriptors.length}
        rowsPerPage={content.rowsPerPage}
        page={content.page}
        onPageChange={(_event, newPage) => setContent(state => state.withPage(newPage))}
        onRowsPerPageChange={(event: React.ChangeEvent<HTMLInputElement>) => setContent(state => state.withRowsPerPage(parseInt(event.target.value, 10)))}
      />
    </Paper>
  </Provider>
  );
}

export default DescriptorTable;

