import React from 'react'

import { FormattedMessage, useIntl } from 'react-intl'
import { ListItemText, InputLabel, Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';

import * as Burger from '@dxs-ts/eveli-primitives';
import { HdesApi } from '@dxs-ts/wrench-api';
import { CancelButton, ConfirmDialog } from '@dxs-ts/eveli-primitives';


type OperationType = "MOVE_ROW" | "DELETE_ROW" | "MOVE_COLUMN" | "DELETE_COLUMN" | "EXPRESSION_COLUMN";

interface DelegateProps {
  decision: HdesApi.AstDecision;
  onChange: (commands: HdesApi.AstCommand) => void;
  setConfirmDelete: (value: { type: 'ROW' | 'COLUMN'; id: string }) => void;
}

interface OrderEditProps {
  decision:HdesApi.AstDecision;
  onClose: () => void;
  onChange: (commands:HdesApi.AstCommand[]) => void;
}


const ExpressionColumn: React.FC<DelegateProps> = ({ decision, onChange }) => {
  const [column, setColumn] = React.useState<string>();
  const [expression, setExpression] = React.useState<string>('');
  const headers = decision.headers.acceptDefs;
  const type = column ? headers.filter(h => h.id === column)[0].valueType : undefined;

  const handleColumn = (column: string) => {
    if (expression) {
      onChange({ id: column, type: "SET_HEADER_EXPRESSION", value: expression });
    }
    setColumn(column);
  }
  const handleExpression = (expression: string) => {
    if (column) {
      onChange({ id: column, type: "SET_HEADER_EXPRESSION", value: expression });
    }
    setExpression(expression);
  }

  return (<>
    <Burger.Select label="decisions.toolbar.organize.action.expression.column"
      selected={expression}
      onChange={handleColumn}
      items={headers.map(v => ({
        id: v.id,
        value: (<ListItemText primary={v.name} />)
      }))} />

    {type ? (
      <Burger.Select label="decisions.toolbar.organize.action.expression"
        selected={expression}
        onChange={handleExpression}
        items={decision.headerExpressions[type].map(v => ({
          id: v,
          value: (<ListItemText primary={v} />)
        }))} />
    ) : null

    }
  </>)
}

const MoveRow: React.FC<DelegateProps> = ({ decision, onChange }) => {
  const [from, setFrom] = React.useState<string>('');
  const [to, setTo] = React.useState<string>('');

  const handleFrom = (from: string) => {
    if (to) {
      onChange({ id: from, type: "INSERT_ROW", value: to });
    }
    setFrom(from);
  }
  const handleTo = (to: string) => {
    if (from) {
      onChange({ id: from, type: "INSERT_ROW", value: to });
    }
    setTo(to);
  }

  return (<>
    <Burger.Select label="decisions.toolbar.organize.action.move.from.row"
      selected={from}
      onChange={handleFrom}
      items={decision.rows.map((v, index) => ({
        id: v.id,
        value: (<ListItemText primary={index} />)
      }))}
    />

    {from ? (
      <Burger.Select label="decisions.toolbar.organize.action.move.to.row"
        selected={to}
        onChange={handleTo}
        items={decision.rows.map((v, index) => ({
          id: v.id,
          value: (<ListItemText primary={`${index}${v.id === from ? ' - selected as source' : ''}`} />)
        }))
        } />
    ) : null
    }
  </>)
}
const DeleteRow: React.FC<DelegateProps> = ({ decision, onChange }) => {

  const [row, setRow] = React.useState<string>('');
  const headers = [...decision.headers.acceptDefs, ...decision.headers.returnDefs];

  let preview = <></>;
  if (row) {
    preview = (<>
      <div>{headers.map(h => h.name).join(";")}</div>
      <div>{decision.rows.filter(v => v.id === row)
        .reduce((_state, v) => v).cells
        .map(c => c.value).join(";")}
      </div>
    </>);
  }

  return (<>
    <Burger.Select
      label="decisions.toolbar.organize.action.deleteRow"
      selected={row}
      onChange={rowId => {
        setRow(rowId);
        onChange({ type: 'DELETE_ROW', id: rowId });
      }}
      items={decision.rows.map((v, index) => ({
        id: v.id,
        value: (<ListItemText primary={index} />)
      }))}
    />
    <div>
      <InputLabel sx={{ paddingBottom: 1 }}><FormattedMessage id='decisions.toolbar.organize.action.deleteRow.contents' /></InputLabel>
      {preview}
    </div>
  </>)
}
const MoveColumn: React.FC<DelegateProps> = ({ onChange, decision }) => {
  const [from, setFrom] = React.useState<string>('');
  const [to, setTo] = React.useState<string>('');
  const headers = [...decision.headers.acceptDefs, ...decision.headers.returnDefs];
  const type = from ? headers.filter(h => h.id === from)[0].direction : undefined;
  const handleFrom = (from: string) => {
    if (to) {
      onChange({ id: from, type: "MOVE_HEADER", value: to });
    }
    setFrom(from);
  }
  const handleTo = (to: string) => {
    if (from) {
      onChange({ id: from, type: "MOVE_HEADER", value: to });
    }
    setTo(to);
  }

  return (<>
    <Burger.Select label="decisions.toolbar.organize.action.move.from.column"
      selected={from}
      onChange={handleFrom}
      items={headers.map(v => ({
        id: v.id,
        value: (<ListItemText primary={v.name} />)
      }))} />

    {type ? (
      <Burger.Select label="decisions.toolbar.organize.action.move.to.column"
        selected={to}
        onChange={handleTo}
        items={headers.filter(r => r.id !== from && r.direction === type).map(v => ({
          id: v.id,
          value: (<ListItemText primary={v.name} />)
        }))} />
    ) : null}
  </>)
}
const DeleteColumn: React.FC<DelegateProps> = ({ decision, onChange }) => {
  const headers = [...decision.headers.acceptDefs, ...decision.headers.returnDefs];
  const [to, setTo] = React.useState<string>('');
  return (<Burger.Select label="decisions.toolbar.organize.action.deleteColumn" helperText="decisions.toolbar.organize.action.deleteColumn.helper"
    onChange={value => {
      setTo(value);
      onChange({ type: 'DELETE_HEADER', id: value });
    }}
    selected={to}
    items={headers.map(v => ({
      id: v.id,
      value: (<ListItemText primary={v.name} />)
    }))}
  />)
}

const OrderEdit: React.FC<OrderEditProps> = (props) => {
  const intl = useIntl();
  const [commands, setCommands] = React.useState<HdesApi.AstCommand>();
  const [operation, setOperation] = React.useState<OperationType | string>('');
  const [confirmDelete, setConfirmDelete] = React.useState<{ type: 'ROW' | 'COLUMN'; id: string } | null>(null);

  const delegate: DelegateProps = {
    onChange: setCommands,
    decision: props.decision,
    setConfirmDelete
  };  
  const operations: Record<OperationType, React.ReactElement> = {
    "EXPRESSION_COLUMN": (<ExpressionColumn {...delegate} />),
    "MOVE_ROW": (<MoveRow {...delegate} />),
    "DELETE_ROW": (<DeleteRow {...delegate} />),
    "MOVE_COLUMN": (<MoveColumn {...delegate} />),
    "DELETE_COLUMN": (<DeleteColumn {...delegate} />),
  };

  const editor = (<>
    <Burger.Select label="decisions.toolbar.organize.action" helperText="decisions.toolbar.organize.action.helper"
      selected={operation}
      onChange={setOperation}
      empty={{ id: '', label: 'decisions.toolbar.organize.action' }}
      items={Object.keys(operations).map((type) => ({
        id: type,
        value: (<ListItemText primary={type} />)
      }))}
    />
    {operation ? operations[operation as OperationType] : null}
  </>);

  return (
    <Dialog open={true} onClose={props.onClose}>
      <DialogTitle><FormattedMessage id='decisions.toolbar.organize.rows.columns' /></DialogTitle>
      <DialogContent>{editor}</DialogContent>
      <DialogActions>
        <CancelButton onClick={props.onClose} />
        <Button onClick={() => {
          if (!commands) {
            props.onClose();
            return;
          }

          if ((commands.type === 'DELETE_ROW' || commands.type === 'DELETE_HEADER') && commands.id) {
            setConfirmDelete({
              type: commands.type === 'DELETE_ROW' ? 'ROW' : 'COLUMN',
              id: commands.id
            });
          } else {
            props.onChange([commands]);
            props.onClose();
          }          
        }}>
          <FormattedMessage id='buttons.apply'/>
        </Button>
      </DialogActions>
      <ConfirmDialog
        open={!!confirmDelete}
        title={intl.formatMessage({ id: "decisions.deleteConfirmTitle" })}
        message={intl.formatMessage(
          { id: "decisions.deleteConfirmText" },
          {
            type: intl.formatMessage({
              id: confirmDelete?.type === 'ROW'
                ? 'decisions.type.row'
                : 'decisions.type.column'
            })
          }
        )}
        confirmLabel={intl.formatMessage({ id: 'button.confirmDelete' })}
        onCancel={() => setConfirmDelete(null)}
        onConfirm={() => {
          if (!confirmDelete) return;
          const { type, id } = confirmDelete;
          const cmd: HdesApi.AstCommand = type === 'ROW'
            ? { type: 'DELETE_ROW', id }
            : { type: 'DELETE_HEADER', id };
          props.onChange([cmd]);
          setConfirmDelete(null);
          props.onClose();
        }}
      />
    </Dialog>
  );
}

export type { OrderEditProps };
export { OrderEdit };
