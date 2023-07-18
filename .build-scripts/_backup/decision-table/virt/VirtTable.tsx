import React from 'react';
import clsx from 'clsx';

import { createStyles, makeStyles } from '@material-ui/styles';
import { TableCell, Paper, Theme } from '@material-ui/core';

import { Column, Table } from 'react-virtualized';

declare module '@material-ui/styles/withStyles' {
  // Augment the BaseCSSProperties so that we can control jss-rtl
  interface BaseCSSProperties {
    /*
     * Used to control if the rule-set should be affected by rtl transformation
     */
    flip?: boolean;
  }
}

const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    flexContainer: {
      display: 'flex',
      alignItems: 'center',
      boxSizing: 'border-box',
    },
    table: {
      // temporary right-to-left patch, waiting for
      // https://github.com/bvaughn/react-virtualized/issues/454
      '& .ReactVirtualized__Table__headerRow': {
        flip: false,
        paddingRight: theme.direction === 'rtl' ? '0 !important' : undefined,
      },
    },
    tableRow: {
      cursor: 'pointer',
    },
    tableRowHover: {
      '&:hover': {
        backgroundColor: theme.palette.primary.main,
      },
    },
    tableCell: {
      flex: 1,
    },
    noClick: {
      cursor: 'initial',
    },
  })
);


interface VirtTableProps {
  data: { columns: number[]; rows: number[]; };
  size: {
    table: { height: number; width: number };
    header: { height: number };
    row: { height: number };
  };
  events: {
    onRowClick: () => void;
    onCellClick: (props: { column: number, row: number }) => void;
  }
  components: {
    cell: (props: { column: number, row: number }) => React.ReactElement;
    header: (key: number) => React.ReactElement;
  }
};


const VirtTable: React.FC<VirtTableProps> = (props) => {
  const classes = useStyles();

  const getRowClassName = (props: { index: number }) => clsx(
    classes.tableRow,
    classes.flexContainer,
    { [classes.tableRowHover]: props.index !== -1 }
  );

  return (
    <Paper style={props.size.table}>
      <Table height={props.size.table.height} width={props.size.table.width}
        rowHeight={props.size.row.height}
        headerHeight={props.size.header.height}
        gridStyle={{ direction: 'inherit' }}
        className={classes.table}
        rowCount={props.data.rows.length}
        rowClassName={getRowClassName}
        rowGetter={({ index }) => props.data.rows[index]}
      >
        {props.data.columns.map((c) => (
          <Column key={c} dataKey={c + ""}
            className={classes.flexContainer}
            width={props.size.table.width / props.data.columns.length}
            headerRenderer={() => (
              <TableCell component="div" variant="head"
                className={clsx(classes.tableCell, classes.flexContainer, classes.noClick)}
                style={{ height: props.size.header.height }}
              >
                {props.components.header(c)}
              </TableCell>

            )}
            cellRenderer={({ rowIndex }) => (
              <TableCell variant="body" component="div"
                onClick={() => props.events.onCellClick({ column: c, row: rowIndex })}
                className={clsx(classes.tableCell, classes.flexContainer)}
                style={{ height: props.size.row.height }}>

                {props.components.cell({ column: c, row: rowIndex })}
              </TableCell>
            )}
          />)
        )}
      </Table>

    </Paper>
  );
}

export type { VirtTableProps };
export { VirtTable }
