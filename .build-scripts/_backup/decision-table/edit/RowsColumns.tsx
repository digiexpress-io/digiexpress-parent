import React from 'react'

import { FormattedMessage } from 'react-intl'
import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, Button, FormControl, InputLabel, Select, MenuItem } from '@material-ui/core';

import { Hdes } from '../deps';
import { Dialog } from '../dialog';


const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {

    },
    button: {
      marginRight: theme.spacing(1),
    },
    dialog: {
      marginTop: theme.spacing(1),
    },
  }),
);

const ExpressionColumn: React.FC<DelegateProps> = ({ children, onChange }) => {
  const [column, setColumn] = React.useState<string>();
  const [expression, setExpression] = React.useState<string>();
  const type = column ? children.headers.filter(h => h.id === column)[0].value : undefined;

  const handleColumn = (column: string) => {
    if(expression) {
      onChange({id: column, type: "SET_HEADER_EXPRESSION", value: expression});
    }
    setColumn(column);
  }
  const handleExpression = (expression: string) => {
    if(column) {
      onChange({id: column, type: "SET_HEADER_EXPRESSION", value: expression});
    }
    setExpression(expression);
  }

  return (<>
    <FormControl variant="filled" fullWidth sx={{ paddingBottom: 1 }}>
      <InputLabel><FormattedMessage id='dt.edit.row.columns.operation.expression.column' /></InputLabel>
      <Select autoWidth
        label={<FormattedMessage id='dt.edit.row.columns.operation.expression.column' />}
        onChange={({ target }) => handleColumn(target.value as string)}>
        {children.headers.filter(h => h.direction === "IN").map(v => (<MenuItem key={v.id} value={v.id}>{v.name}</MenuItem>))}
      </Select>
    </FormControl>
    { type ? (
      <FormControl variant="filled" fullWidth sx={{ paddingBottom: 1 }}>
        <InputLabel><FormattedMessage id='dt.edit.row.columns.operation.expression.value' /></InputLabel>
        <Select autoWidth
          label={<FormattedMessage id='dt.edit.row.columns.operation.expression.value' />}
          onChange={({ target }) => handleExpression(target.value as string)}>
          {children.headerExpressions[type].map(v => (<MenuItem key={v} value={v}>{v}</MenuItem>))}
        </Select>
      </FormControl>
    ) : null

    }
  </>)
}

const MoveRow: React.FC<DelegateProps> = ({ children, onChange }) => {
  const [from, setFrom] = React.useState<string>();
  const [to, setTo] = React.useState<string>();

  const handleFrom = (from: string) => {
    if(to) {
      onChange({id: from, type: "INSERT_ROW", value: to});
    }
    setFrom(from);
  }
  const handleTo = (to: string) => {
    if(from) {
      onChange({id: from, type: "INSERT_ROW", value: to});
    }
    setTo(to);
  }

  return (<>
    <FormControl variant="filled" fullWidth sx={{ paddingBottom: 1 }}>
      <InputLabel><FormattedMessage id='dt.edit.row.columns.operation.move.from.row' /></InputLabel>
      <Select autoWidth
        label={<FormattedMessage id='dt.edit.row.columns.operation.move.from.row' />}
        onChange={({ target }) => handleFrom(target.value as string)}>
        {children.rows.map(v => (<MenuItem key={v.id} value={v.id}>{v.order}</MenuItem>))}
      </Select>
    </FormControl>
    { from ? (
      <FormControl variant="filled" fullWidth sx={{ paddingBottom: 1 }}>
        <InputLabel><FormattedMessage id='dt.edit.row.columns.operation.move.to.row' /></InputLabel>
        <Select autoWidth
          label={<FormattedMessage id='dt.edit.row.columns.operation.move.to.row' />}
          onChange={({ target }) => handleTo(target.value as string)}>
          {children.rows.filter(r => r.id !== from).map(v => (<MenuItem key={v.id} value={v.id}>{v.order}</MenuItem>))}
        </Select>
      </FormControl>
    ) : null

    }
  </>)
}
const DeleteRow: React.FC<DelegateProps> = ({ children, onChange }) => {

  const [row, setRow] = React.useState<number>();

  let preview;
  if (row) {
    const headers = children.headers.map(h => h.name).join(";");
    preview = (<>
      <div>{headers}</div>
      <div>{children.rows[row].cells.map(c => c.value).join(";")}</div>
    </>);
  }

  return (<>
    <FormControl variant="filled" fullWidth sx={{ paddingBottom: 1 }}>
      <InputLabel><FormattedMessage id='dt.edit.row.columns.operation.delete.row' /></InputLabel>
      <Select autoWidth
        label={<FormattedMessage id='dt.edit.row.columns.operation.delete.row' />}
        onChange={({ target }) => {
          const index = target.value as any;
          setRow(index)
          onChange({ id: children.rows[index].id, type: "DELETE_ROW" });
        }}
      >
        {Object.keys(children.rows).map(v => (<MenuItem key={v} value={v}>{v}</MenuItem>))}
      </Select>
    </FormControl>

    <div>
      <InputLabel sx={{ paddingBottom: 1 }}><FormattedMessage id='dt.edit.row.columns.operation.delete.row.preview' /></InputLabel>
      {preview}
    </div>
  </>)
}
const MoveColumn: React.FC<DelegateProps> = ({ onChange, children }) => {
  const [from, setFrom] = React.useState<string>();
  const [to, setTo] = React.useState<string>();
  const type = from ? children.headers.filter(h => h.id === from)[0].direction : undefined;
  const handleFrom = (from: string) => {
    if(to) {
      onChange({id: from, type: "MOVE_HEADER", value: to});
    }
    setFrom(from);
  }
  const handleTo = (to: string) => {
    if(from) {
      onChange({id: from, type: "MOVE_HEADER", value: to});
    }
    setTo(to);
  }

  return (<>
    <FormControl variant="filled" fullWidth sx={{ paddingBottom: 1 }}>
      <InputLabel><FormattedMessage id='dt.edit.row.columns.operation.move.from.column' /></InputLabel>
      <Select autoWidth
        label={<FormattedMessage id='dt.edit.row.columns.operation.move.from.column' />}
        onChange={({ target }) => handleFrom(target.value as string)}>
        {children.headers.map(v => (<MenuItem key={v.id} value={v.id}>{v.name}</MenuItem>))}
      </Select>
    </FormControl>
    { type ? (
      <FormControl variant="filled" fullWidth sx={{ paddingBottom: 1 }}>
        <InputLabel><FormattedMessage id='dt.edit.row.columns.operation.move.to.column' /></InputLabel>
        <Select autoWidth
          label={<FormattedMessage id='dt.edit.row.columns.operation.move.to.column' />}
          onChange={({ target }) => handleTo(target.value as string)}>
          {children.headers.filter(r => r.id !== from && r.direction === type).map(v => (<MenuItem key={v.id} value={v.id}>{v.name}</MenuItem>))}
        </Select>
      </FormControl>
    ) : null

    }
  </>)
}
const DeleteColumn: React.FC<DelegateProps> = ({ children, onChange }) => {

  return (<FormControl variant="filled" fullWidth sx={{ paddingBottom: 1 }}>
    <InputLabel><FormattedMessage id='dt.edit.row.columns.operation.delete.column' /></InputLabel>
    <Select autoWidth
      onChange={({ target }) => {
        onChange({ id: target.value as any, type: "DELETE_HEADER" });
      }}
      label={<FormattedMessage id='dt.edit.row.columns.operation.delete.column' />}>
      {Object.values(children.headers).map(v => (<MenuItem key={v.id} value={v.id}>{v.name}</MenuItem>))}
    </Select>
  </FormControl>)
}

type OperationType = "MOVE_ROW" | "DELETE_ROW" | "MOVE_COLUMN" | "DELETE_COLUMN" | "EXPRESSION_COLUMN";

interface DelegateProps {
  children: Hdes.AstAPI.Dt;
  onChange: (commands: Hdes.AstAPI.DtCommand) => void;
}

interface RowsColumnsProps {
  children: Hdes.AstAPI.Dt;
  onClose: () => void;
  onChange: (commands: Hdes.AstAPI.DtCommand[]) => void;
}

const RowsColumns: React.FC<RowsColumnsProps> = (props) => {
  const classes = useStyles();
  const [commands, setCommands] = React.useState<Hdes.AstAPI.DtCommand>();
  const [operation, setOperation] = React.useState<OperationType>();

  const delegate: DelegateProps = { onChange: setCommands, children: props.children };
  const operations: Record<OperationType, React.ReactElement> = {
    "EXPRESSION_COLUMN": (<ExpressionColumn {...delegate} />),
    "MOVE_ROW": (<MoveRow {...delegate} />),
    "DELETE_ROW": (<DeleteRow {...delegate} />),
    "MOVE_COLUMN": (<MoveColumn {...delegate} />),
    "DELETE_COLUMN": (<DeleteColumn {...delegate} />),
  };

  const editor = (<>
    <FormControl variant="filled" fullWidth sx={{ paddingBottom: 1 }}>
      <InputLabel><FormattedMessage id='dt.edit.row.columns.operation' /></InputLabel>
      <Select autoWidth
        value={operation}
        onChange={({ target }) => setOperation(target.value)}
        label={<FormattedMessage id='dt.edit.row.columns.operation' />}>
        {Object.keys(operations).map(v => (<MenuItem key={v} value={v}>{v}</MenuItem>))}
      </Select>
    </FormControl>
    {operation ? operations[operation] : null}
  </>);

  const handleSave = () => {
    if (commands) {
      props.onChange([commands]);
    }
    props.onClose();
  }

  const actions = (<>
    <Button color="secondary" className={classes.button} onClick={props.onClose}><FormattedMessage id={"dt.cell.edit.button.cancel"} /></Button>
    <Button variant="contained" color="primary" className={classes.button} onClick={handleSave}><FormattedMessage id={"dt.cell.edit.button.confirm"} /></Button>
  </>);

  return (<Dialog content={<div className={classes.dialog}>{editor}</div>} onClose={props.onClose}
    actions={actions}
    title={<span><FormattedMessage id='dt.edit.row.columns.title' /></span>}
    open={true} />);
}

export type { RowsColumnsProps };
export default RowsColumns;
