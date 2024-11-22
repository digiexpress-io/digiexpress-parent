import React, { useContext } from 'react';
import {
  Table, TableContainer, Paper, TableHead, TableRow, TableBody,
  TableCell, Grid, styled
} from '@mui/material';
import type { ItemProps } from './componentTypes';
import { GFormReviewContext } from './GFormReviewContext';
import { RowGroupContext } from './GFormReviewRowGroupContext';
import { GroupContext } from './GFormReviewGroupContext';

const StyledTableCell = styled(TableCell)({
  whiteSpace: 'nowrap',
  fontWeight: '450',
  fontSize: '1rem',
});

const GroupTableGrid: React.FC<{ headers: any, rows: JSX.Element[] | undefined }> = ({ headers, rows }) => {
  return (
    <Grid data-type='group-table-grid' container spacing={2}>
      <Grid item xs={12}>
        <TableContainer data-type='group-table' component={Paper}>
          <Table>
            <TableHead >
              <TableRow>
                {headers}
              </TableRow>
            </TableHead>
            <TableBody>
              {rows}
            </TableBody>
          </Table>
        </TableContainer>
      </Grid>
    </Grid>
  );
}

export const GFormReviewRowGroup: React.FC<ItemProps> = ({ item }) => {
  const dC = useContext(GFormReviewContext);;
  const groupCtx = useContext(GroupContext);

  const getRowIds = (item: { id: string }) => {
    let result: string[] = [];
    const answer = dC.getAnswer(item.id);
    if (answer) {
      answer.forEach((a: string) => {
        result.push(`${item.id}.${a}`);
      });
    }
    return result;
  };

  const headers = item.items ? item.items.map((id: string) => {
    const headerItem: { label: Record<string, string> | any } = dC.getItem(id) as any;
    const children = dC.getTranslated(headerItem.label)

    return <StyledTableCell key={id}>{children}</StyledTableCell>

  }) : null

  const rowIds = getRowIds(item);
  let rows: JSX.Element[] | null = null;
  if (rowIds && headers) {
    rows = rowIds.map(rowId => <TableRow data-type='group-table-row' key={rowId}>
      {item.items.map((id: string) => <TableCell key={id}>
        {dC.createItem(id, `${rowId}.${id}`)}
      </TableCell>)}
    </TableRow>);
  }

  if (!rows || rows.length === 0) {
    return null;
  }

  return (
    <GroupContext.Provider value={{ level: groupCtx.level < 4 ? groupCtx.level + 1 : groupCtx.level }}>
      <RowGroupContext.Provider value={true}>
        <GroupTableGrid headers={headers} rows={rows} />
      </RowGroupContext.Provider>
    </GroupContext.Provider>
  );
}
