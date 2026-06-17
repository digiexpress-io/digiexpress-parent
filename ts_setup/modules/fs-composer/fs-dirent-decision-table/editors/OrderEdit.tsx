import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, InputLabel, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs } from '@dxs-ts/fs-api';
import { FsDirentSelectSingle } from '../../fs-utilities';
import { FsDirentButtonCancel } from '../../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../../fs-dirent-button-save';

type OperationType = 'MOVE_ROW' | 'DELETE_ROW' | 'MOVE_HEADER' | 'DELETE_HEADER' | 'SET_HEADER_EXPRESSION' | 'SET_HEADER_DIRECTION';

interface DelegateProps {
  decision: Fs.DecisionAst;
  onChange: (command: Fs.AstCommand) => void;
  setConfirmDelete: (value: { type: 'ROW' | 'COLUMN'; id: string }) => void;
}

interface OrderEditProps {
  decision: Fs.DecisionAst;
  onClose: () => void;
  onChange: (commands: Fs.AstCommand[]) => void;
}

const ExpressionColumn: React.FC<DelegateProps> = ({ decision, onChange }) => {
  const intl = useIntl();
  const [column, setColumn] = React.useState<string>('');
  const [expression, setExpression] = React.useState<string>('');
  const headers = decision.headers.acceptDefs;
  const type = column ? headers.find(h => h.id === column)?.valueType : undefined;

  const handleColumn = (col: string) => {
    if (expression) {
      onChange({ id: col, type: 'SET_HEADER_EXPRESSION', value: expression });
    }
    setColumn(col);
  };

  const handleExpression = (expr: string) => {
    if (column) {
      onChange({ id: column, type: 'SET_HEADER_EXPRESSION', value: expr });
    }
    setExpression(expr);
  };

  return (
    <>
      <Box sx={{ mb: 1 }}>
        <InputLabel>{intl.formatMessage({ id: 'decisions.toolbar.organize.action.expression.column' })}</InputLabel>
        <FsDirentSelectSingle
          value={column}
          onChange={handleColumn}
          options={headers.map(v => ({ value: v.id, label: v.name }))}
        />
      </Box>
      {type ? (
        <Box sx={{ mb: 1 }}>
          <InputLabel>{intl.formatMessage({ id: 'decisions.toolbar.organize.action.expression' })}</InputLabel>
          <FsDirentSelectSingle
            value={expression}
            onChange={handleExpression}
            options={(decision.headerExpressions[type] ?? []).map(v => ({ value: v, label: v }))}
          />
        </Box>
      ) : null}
    </>
  );
};

const MoveRow: React.FC<DelegateProps> = ({ decision, onChange }) => {
  const intl = useIntl();
  const [from, setFrom] = React.useState<string>('');
  const [to, setTo] = React.useState<string>('');

  const handleFrom = (f: string) => {
    if (to) {
      onChange({ id: f, type: 'INSERT_ROW', value: to });
    }
    setFrom(f);
  };

  const handleTo = (t: string) => {
    if (from) {
      onChange({ id: from, type: 'INSERT_ROW', value: t });
    }
    setTo(t);
  };

  return (
    <>
      <Box sx={{ mb: 1 }}>
        <InputLabel>{intl.formatMessage({ id: 'decisions.toolbar.organize.action.move.from.row' })}</InputLabel>
        <FsDirentSelectSingle
          value={from}
          onChange={handleFrom}
          options={decision.rows.map((v, index) => ({ value: v.id, label: String(index) }))}
        />
      </Box>
      {from ? (
        <Box sx={{ mb: 1 }}>
          <InputLabel>{intl.formatMessage({ id: 'decisions.toolbar.organize.action.move.to.row' })}</InputLabel>
          <FsDirentSelectSingle
            value={to}
            onChange={handleTo}
            options={decision.rows.map((v, index) => ({
              value: v.id,
              label: `${index}${v.id === from ? ' - selected as source' : ''}`
            }))}
          />
        </Box>
      ) : null}
    </>
  );
};

const DeleteRow: React.FC<DelegateProps> = ({ decision, onChange }) => {
  const intl = useIntl();
  const [row, setRow] = React.useState<string>('');
  const headers = [...decision.headers.acceptDefs, ...decision.headers.returnDefs];

  const preview = row ? (
    <>
      <div>{headers.map(h => h.name).join(';')}</div>
      <div>{decision.rows.find(v => v.id === row)?.cells.map(c => c.value).join(';')}</div>
    </>
  ) : null;

  return (
    <>
      <Box sx={{ mb: 1 }}>
        <InputLabel>{intl.formatMessage({ id: 'decisions.toolbar.organize.action.deleteRow' })}</InputLabel>
        <FsDirentSelectSingle
          value={row}
          onChange={rowId => {
            setRow(rowId);
            onChange({ type: 'DELETE_ROW', id: rowId });
          }}
          options={decision.rows.map((v, index) => ({ value: v.id, label: String(index) }))}
        />
      </Box>
      {preview ? (
        <InputLabel sx={{ p: 2 }}>
          {intl.formatMessage({ id: 'decisions.toolbar.organize.action.deleteRow.contents' })}:
          {preview}
        </InputLabel>
      ) : null}
    </>
  );
};

const MoveColumn: React.FC<DelegateProps> = ({ decision, onChange }) => {
  const intl = useIntl();
  const [from, setFrom] = React.useState<string>('');
  const [to, setTo] = React.useState<string>('');
  const headers = [...decision.headers.acceptDefs, ...decision.headers.returnDefs];
  const type = from ? headers.find(h => h.id === from)?.direction : undefined;

  const handleFrom = (f: string) => {
    if (to) {
      onChange({ id: f, type: 'MOVE_HEADER', value: to });
    }
    setFrom(f);
  };

  const handleTo = (t: string) => {
    if (from) {
      onChange({ id: from, type: 'MOVE_HEADER', value: t });
    }
    setTo(t);
  };

  return (
    <>
      <Box sx={{ mb: 1 }}>
        <InputLabel>{intl.formatMessage({ id: 'decisions.toolbar.organize.action.move.from.column' })}</InputLabel>
        <FsDirentSelectSingle
          value={from}
          onChange={handleFrom}
          options={headers.map(v => ({ value: v.id, label: v.name }))}
        />
      </Box>
      {type ? (
        <Box sx={{ mb: 1 }}>
          <InputLabel>{intl.formatMessage({ id: 'decisions.toolbar.organize.action.move.to.column' })}</InputLabel>
          <FsDirentSelectSingle
            value={to}
            onChange={handleTo}
            options={headers.filter(r => r.id !== from && r.direction === type).map(v => ({ value: v.id, label: v.name }))}
          />
        </Box>
      ) : null}
    </>
  );
};

const DeleteColumn: React.FC<DelegateProps> = ({ decision, onChange }) => {
  const intl = useIntl();
  const [selected, setSelected] = React.useState<string>('');
  const headers = [...decision.headers.acceptDefs, ...decision.headers.returnDefs];

  return (
    <Box sx={{ mb: 1 }}>
      <InputLabel>{intl.formatMessage({ id: 'decisions.toolbar.organize.action.deleteColumn' })}</InputLabel>
      <FsDirentSelectSingle
        value={selected}
        onChange={value => {
          setSelected(value);
          onChange({ type: 'DELETE_HEADER', id: value });
        }}
        options={headers.map(v => ({ value: v.id, label: v.name }))}
      />
    </Box>
  );
};

const DirectionColumn: React.FC<DelegateProps> = ({ decision, onChange }) => {
  const intl = useIntl();
  const [column, setColumn] = React.useState<string>('');
  const headers = [...decision.headers.acceptDefs, ...decision.headers.returnDefs];
  const selectedHeader = column ? headers.find(h => h.id === column) : undefined;

  const handleColumn = (col: string) => {
    const header = headers.find(h => h.id === col);
    if (header) {
      const opposite: 'IN' | 'OUT' = header.direction === 'IN' ? 'OUT' : 'IN';
      onChange({ id: col, type: 'SET_HEADER_DIRECTION', value: opposite });
    }
    setColumn(col);
  };

  return (
    <>
      <Box sx={{ mb: 1 }}>
        <InputLabel>{intl.formatMessage({ id: 'decisions.toolbar.organize.action.direction.column' })}</InputLabel>
        <FsDirentSelectSingle
          value={column}
          onChange={handleColumn}
          options={headers.map(v => ({ value: v.id, label: `${v.name} (${v.direction})` }))}
        />
      </Box>
      {selectedHeader ? (
        <InputLabel sx={{ pt: 2, pl: 2 }}>
          {intl.formatMessage({ id: 'decisions.toolbar.organize.action.direction.change' }, { newDirection: selectedHeader.direction === 'IN' ? 'OUT' : 'IN' })}
        </InputLabel>
      ) : null}
    </>
  );
};

const OrderEdit: React.FC<OrderEditProps> = (props) => {
  const intl = useIntl();
  const [command, setCommand] = React.useState<Fs.AstCommand | undefined>();
  const [operation, setOperation] = React.useState<string>('');
  const [confirmDelete, setConfirmDelete] = React.useState<{ type: 'ROW' | 'COLUMN'; id: string } | null>(null);

  const delegate: DelegateProps = {
    onChange: setCommand,
    decision: props.decision,
    setConfirmDelete,
  };

  const operations: Record<OperationType, React.ReactElement> = {
    SET_HEADER_EXPRESSION: <ExpressionColumn {...delegate} />,
    MOVE_ROW: <MoveRow {...delegate} />,
    DELETE_ROW: <DeleteRow {...delegate} />,
    MOVE_HEADER: <MoveColumn {...delegate} />,
    DELETE_HEADER: <DeleteColumn {...delegate} />,
    SET_HEADER_DIRECTION: <DirectionColumn {...delegate} />,
  };

  const helperText = operation ? `decisions.toolbar.organize.helper.${operation}` : 'decisions.toolbar.organize.action.helper';

  return (
    <>
      <Dialog open={true} onClose={props.onClose}>
        <DialogTitle>{intl.formatMessage({ id: 'decisions.toolbar.organize.rows.columns' })}</DialogTitle>
        <DialogContent sx={{ minWidth: 400 }}>
          <Box sx={{ mb: 1 }}>
            <InputLabel>{intl.formatMessage({ id: 'decisions.toolbar.organize.action' })}</InputLabel>
            <FsDirentSelectSingle
              value={operation}
              onChange={op => { setOperation(op); setCommand(undefined); }}
              options={(Object.keys(operations) as OperationType[]).map(type => ({
                value: type,
                label: intl.formatMessage({ id: `decisions.toolbar.organize.operation.${type}` }),
              }))}
            />
            <InputLabel sx={{ mt: 0.5, fontSize: '0.75rem' }}>
              {intl.formatMessage({ id: helperText })}
            </InputLabel>
          </Box>
          {operation ? operations[operation as OperationType] : null}
        </DialogContent>
        <DialogActions>
          <FsDirentButtonCancel onClick={props.onClose} />
          <FsDirentButtonSave onClick={() => {
            if (!command) {
              props.onClose();
              return;
            }
            if ((command.type === 'DELETE_ROW' || command.type === 'DELETE_HEADER') && command.id) {
              setConfirmDelete({
                type: command.type === 'DELETE_ROW' ? 'ROW' : 'COLUMN',
                id: command.id,
              });
            } else {
              props.onChange([command]);
              props.onClose();
            }
          }} />
        </DialogActions>
      </Dialog>

      <Dialog open={!!confirmDelete} onClose={() => setConfirmDelete(null)}>
        <DialogTitle>{intl.formatMessage({ id: 'decisions.deleteConfirmTitle' })}</DialogTitle>
        <DialogContent>
          {intl.formatMessage(
            { id: 'decisions.deleteConfirmText' },
            {
              type: intl.formatMessage({
                id: confirmDelete?.type === 'ROW' ? 'decisions.type.row' : 'decisions.type.column'
              })
            }
          )}
        </DialogContent>
        <DialogActions>
          <FsDirentButtonCancel onClick={() => setConfirmDelete(null)} />
          <FsDirentButtonSave onClick={() => {
            if (!confirmDelete) {
              return;
            };
            const { type, id } = confirmDelete;
            const cmd: Fs.AstCommand = type === 'ROW' ? { type: 'DELETE_ROW', id } : { type: 'DELETE_HEADER', id };
            props.onChange([cmd]);
            setConfirmDelete(null);
            props.onClose();
          }} />
        </DialogActions>
      </Dialog>
    </>
  );
};

export type { OrderEditProps };
export { OrderEdit };
