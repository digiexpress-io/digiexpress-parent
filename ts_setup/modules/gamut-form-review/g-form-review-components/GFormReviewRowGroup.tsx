import React from 'react';
import {
  Table, TableContainer, Paper, TableHead, TableRow, TableBody,
  TableCell, Grid2, styled,
  generateUtilityClass,
  useThemeProps
} from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

import type { ItemProps } from './componentTypes';
import { GFormReviewContext } from './GFormReviewContext';
import { RowGroupContext } from './GFormReviewRowGroupContext';
import { GroupContext } from './GFormReviewGroupContext';



const MUI_NAME = 'GFormReviewRowGroup';

export interface GFormReviewRowGroupClasses {
  root: string,
  tableHeaders: string
}

export type GFormReviewRowGroupClassKey = keyof GFormReviewRowGroupClasses;


const GroupTableGrid: React.FC<{ headers: any, rows: JSX.Element[] | undefined }> = ({ headers, rows }) => {
  return (
    <Grid2 data-type='group-table-grid'>
      <Grid2 size={{ xs: 12 }}>
        <TableContainer data-type='group-table' component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                {headers}
              </TableRow>
            </TableHead>
            <TableBody>
              {rows}
            </TableBody>
          </Table>
        </TableContainer>
      </Grid2>
    </Grid2>
  );
}

export const GFormReviewRowGroup: React.FC<ItemProps> = (initProps) => {
  const dC = React.useContext(GFormReviewContext);;
  const groupCtx = React.useContext(GroupContext);



  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);


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

  const headers = props.item.items ? props.item.items.map((id: string) => {
    const headerItem: { label: Record<string, string> | any } = dC.getItem(id) as any;
    const children = dC.getTranslated(headerItem.label)

    return <TableCell key={id} className={classes.tableHeaders}>{children}</TableCell>

  }) : null

  const rowIds = getRowIds(props.item);
  let rows: JSX.Element[] | null = null;
  if (rowIds && headers) {
    rows = rowIds.map(rowId => <TableRow data-type='group-table-row' key={rowId}>
      {props.item.items.map((id: string) => <TableCell key={id}>
        {dC.createItem(id, `${rowId}.${id}`)}
      </TableCell>)}
    </TableRow>);
  }

  if (!rows || rows.length === 0) {
    return null;
  }
  const Root = props.component ?? GFormReviewRowGroupRoot;
  return (
    <GroupContext.Provider value={{ level: groupCtx.level < 4 ? groupCtx.level + 1 : groupCtx.level }}>
      <RowGroupContext.Provider value={true}>
        <Root className={classes.root} ownerState={props}>
          <GroupTableGrid headers={headers} rows={rows} />
        </Root>
      </RowGroupContext.Provider>
    </GroupContext.Provider>
  );
}



const useUtilityClasses = (ownerState: ItemProps) => {

  const slots = {
    root: ['root', ownerState.item.id],
    tableHeaders: ['tableHeaders']
  };

  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


const GFormReviewRowGroupRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})(({ theme }) => {
  return {
    'GFormReviewRowGroup-tableHeaders': {
      whiteSpace: 'nowrap',
      fontWeight: '450',
      fontSize: '1rem',
    }
  };
});

