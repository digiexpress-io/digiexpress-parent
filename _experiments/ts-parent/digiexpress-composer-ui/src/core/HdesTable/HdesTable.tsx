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


import HdesTableHeader from './HdesTableHeader';
import HdesTableRow from './HdesTableRow';


function init(tree: DeClient.HdesTree): DeClient.TablePagination<DeClient.HdesBodyEntity> {
  return new DeClient.TablePaginationImpl<DeClient.HdesBodyEntity>({
    src: [
      ...Object.values(tree.decisions).map(e => new DeClient.HdesBodyEntityImpl(e)), 
      ...Object.values(tree.flows).map(e => new DeClient.HdesBodyEntityImpl(e)),
      ...Object.values(tree.services).map(e => new DeClient.HdesBodyEntityImpl(e)),
    ], 
    orderBy: 'id', 
    sorted: false })
}


const Loading: React.FC<{}> = ({ }) => {
  const client = DeClient.useService();
  return <>...Loading: {client.config.url}</>
}

const StyledTable: React.FC<{tree: DeClient.HdesTree}> = ({ tree }) => {
  const [content, setContent] = React.useState<DeClient.TablePagination<DeClient.HdesBodyEntity>>(init(tree));

  return (<>
    <TableContainer>
      <Table size='small'>
        <TableHead><HdesTableHeader content={content} setContent={setContent} /></TableHead>
        <TableBody>
          {content.entries.map((row) => (<HdesTableRow key={row.id} row={row} tree={tree} />))}
          {content.emptyRows > 0 ? <TableRow style={{ height: (28 + 1) * content.emptyRows }}><TableCell colSpan={6} /></TableRow> : null}
        </TableBody>
      </Table>
    </TableContainer>
    <TablePagination
      rowsPerPageOptions={content.rowsPerPageOptions}
      component="div"
      count={content.entries.length}
      rowsPerPage={content.rowsPerPage}
      page={content.page}
      onPageChange={(_event, newPage) => setContent(state => state.withPage(newPage))}
      onRowsPerPageChange={(event: React.ChangeEvent<HTMLInputElement>) => setContent(state => state.withRowsPerPage(parseInt(event.target.value, 10)))}
    />
  </>);
}

const HdesTable: React.FC<{}> = React.memo(({ }) => {
  const client = DeClient.useService();

  const LoadApps = React.lazy(async () => {
    const tree = await client.hdes();
    return { default: () => <StyledTable tree={tree}/>};
  });

  return (<Paper sx={{ width: '100%', mb: 2 }}>
    <React.Suspense fallback={<Loading/>}><LoadApps /></React.Suspense>
  </Paper>);
});

export default HdesTable;

