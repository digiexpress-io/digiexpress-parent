import React from 'react';

import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, TableContainer, Button, ButtonGroup } from '@material-ui/core';

import { Hdes } from './deps';
import { VirtTable, VirtTableProps } from './virt/VirtTable'
import { DecisionTableToolbar } from './DecisionTableToolbar';
import CellEditor from './edit/cell-editor';
import HeaderEditor from './edit/HeaderEditor';


const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {

    },
    row: {
      paddingRight: theme.spacing(2),
      color: theme.palette.primary.main
    },
    filler: {
      flexGrow: 1
    },
    cellOut: {
      color: theme.palette.secondary.main,
      textTransform: 'none'
    },
    cellIn: {
      textTransform: 'none'
    },
    header: {
      textTransform: 'none',
      width: '100%',
      height: '100%',
    },
    headerName: {
      fontWeight: 'bold',
      textTransform: 'none',
    }
  }),
);


interface EditMode {
  rule?: { header: Hdes.AstAPI.DtHeader, cell: Hdes.AstAPI.DtCell },
  header?: { header: Hdes.AstAPI.DtHeader },
}

interface DecisionTableProps {
  children: Hdes.AstAPI.DtCommand[];
  models: Hdes.ModelAPI.Models;
  ast: (commands: Hdes.AstAPI.DtCommand[], rev?: number) => Promise<Hdes.AstAPI.Dt>;
  onChange: (command: Hdes.AstAPI.DtCommand[]) => void;
  size: { width: number, height: number };
};

const DecisionTable: React.FC<DecisionTableProps> = ({ children, onChange, models, ast, size }) => {
  const classes = useStyles();
  const ref = React.createRef<HTMLDivElement>();
  const [edit, setEdit] = React.useState<EditMode | undefined>();
  const [parser, setParser] = React.useState<{dt: Hdes.AstAPI.Dt, commands: Hdes.AstAPI.DtCommand[]}>();
  const handleCloseEdit = () => setEdit(undefined);  

  React.useEffect(() => {
    if(parser && children.length === parser.commands.length) {
      return;
    }
    ast(children).then((dt) => setParser({dt, commands: children}))
    .catch(e => { console.error(e); })
  }, [children, ast, parser])

  if (!parser) {
    return null;
  }

  const virtProps: VirtTableProps = {
    data: {
      columns: Object.keys(parser.dt.headers) as any,
      rows: Object.keys(parser.dt.rows) as any
    },
    events: {
      onRowClick: () => console.log("row clicked"),
      onCellClick: (props) => {
        const header = parser.dt.headers[props.column];
        const cell = parser.dt.rows[props.row].cells[props.column];
        setEdit({ rule: { header, cell } });
      }
    },
    size: {
      header: { height: 100 },
      row: { height: 40 },
      table: { height: size.height - 64, width: size.width }
    },
    components: {
      cell: (props) => {
        const header = parser.dt.headers[props.column];
        const cell = parser.dt.rows[props.row].cells[props.column];
        const directionStyle = header.direction === "OUT" ? classes.cellOut : classes.cellIn;
        
        return (<>
            {parseInt(props.column + "") === 0 ? <span className={classes.row}>{props.row}</span> : null} 
            <span className={directionStyle}>{cell.value}</span>
          </>);
      },
      header: (pos) => {
        const header = parser.dt.headers[pos];
        const color = header.direction === "OUT" ? "secondary" : "inherit";
        return (
          <ButtonGroup orientation="vertical" variant="text"
            color={color} className={classes.header} 
            onClick={() => setEdit({ header: { header } })}>
            
            <Button>
              <div className={classes.headerName}>{header.name}</div>
            </Button>
            <Button>{header.value}</Button>
          </ButtonGroup>
        );
      },
    }
  }

  const model = models["DT"].filter(m => m.name === parser.dt.name)[0];
  return (<div className={classes.root} ref={ref}>
    {edit?.rule ? <CellEditor dt={parser.dt} cell={edit?.rule.cell} header={edit?.rule.header} onClose={handleCloseEdit} onChange={(command) => onChange([command])} /> : null}
    {edit?.header ? <HeaderEditor dt={parser.dt} header={edit?.header.header} onClose={handleCloseEdit} onChange={onChange} /> : null}
    <DecisionTableToolbar dt={parser.dt} onChange={onChange} model={model} />
    <TableContainer>
      <VirtTable {...virtProps} />
    </TableContainer>
    <div className={classes.filler}></div>
  </div>);
}

export type { DecisionTableProps };
export { DecisionTable };
