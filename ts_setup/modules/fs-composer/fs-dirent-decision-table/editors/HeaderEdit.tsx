import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Stack, Typography } from '@mui/material';
import { Fs } from '@dxs-ts/fs-api';
import { FormattedMessage, useIntl } from 'react-intl';
import { FsDirentSelectSingle, FsDirentTextField } from '../../fs-utilities';
import { FsDirentButtonCancel } from '../../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../../fs-dirent-button-save';
import { FsDirentButtonDelete } from '../../fs-dirent-button-delete';
import { EditValueSet } from './builders/EditValueSet';
import { EditIntlValueSet } from './builders/EditIntlValueSet';

interface HeaderEditProps {
  dt: Fs.DecisionAst;
  header: Fs.TypeDef;
  onClose: () => void;
  onChange: (commands: Fs.AstCommand[]) => void;
}

const addCommand = (command: Fs.AstCommand, commands: Fs.AstCommand[]) => {
  const result: Fs.AstCommand[] = [];
  for (const previous of commands) {
    if (command.type !== previous.type) {
      result.push(previous);
    }
  }
  result.push(command);
  return result;
};

const HeaderEdit: React.FC<HeaderEditProps> = ({ dt, header, onClose, onChange }) => {
  const intl = useIntl();
  const [commands, setCommands] = React.useState<Fs.AstCommand[]>([]);
  const [name, setName] = React.useState<string>(header.name);
  const [exp, setExp] = React.useState<string>('');
  const [script, setScript] = React.useState<string>('');
  const [valueType, setValueType] = React.useState<string>(header.valueType);
  const [valueSet, setValueSet] = React.useState<string[]>(header.valueSet ?? []);
  const expressions = dt.headerExpressions[header.valueType];

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='decisions.header.dialog.title.simple' /></DialogTitle>
      <DialogContent>
        <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 2 }}>
          <FormattedMessage id='decisions.header.dialog.title' values={{ name: dt.name, column: header.name }} />
        </Typography>
        <Stack direction='column' gap={1}>
          <Typography variant='subtitle2'>{intl.formatMessage({ id: 'dt.header.name' })}</Typography>
          <FsDirentTextField
            value={name}
            onChange={(value) => {
              setCommands(addCommand({ type: 'SET_HEADER_REF', id: header.id, value }, commands));
              setName(value);
            }}
          />

          <Typography variant='subtitle2'>{intl.formatMessage({ id: 'dt.header.dataType' })}</Typography>
          <FsDirentSelectSingle
            value={valueType}
            onChange={(value) => {
              setCommands(addCommand({ type: 'SET_HEADER_TYPE', id: header.id, value }, commands));
              setValueType(value);
            }}
            options={dt.headerTypes
              .filter((type) => !(header.direction === 'IN' && type === 'INTL'))
              .map((type) => ({ value: type, label: type }))}
          />

          {valueType === 'INTL' && (
            <Typography variant="caption" color="text.secondary">
              <FormattedMessage id="decisions.intl.columnType.help" />
            </Typography>
          )}

          {valueType === 'INTL' && (
            <EditIntlValueSet
              valueSet={valueSet}
              setValueSet={setValueSet}
              commands={commands}
              setCommands={setCommands}
              headerId={header.id}
            />
          )}

          {valueType === 'STRING' && (
            <EditValueSet
              valueSet={valueSet}
              setValueSet={setValueSet}
              commands={commands}
              setCommands={setCommands}
              headerId={header.id}
            />
          )}

          {header.direction !== 'OUT' && (
            <>
              <Typography variant='subtitle2'>{intl.formatMessage({ id: 'dt.header.expression' })}</Typography>
              <FsDirentSelectSingle
                value={exp}
                onChange={(value) => {
                  setCommands(addCommand({ type: 'SET_HEADER_EXPRESSION', id: header.id, value }, commands));
                  setExp(value);
                }}
                options={(expressions ?? []).map((type) => ({ value: type, label: type }))}
              />
            </>
          )}

          {header.direction !== 'IN' && (
            <>
              <Typography variant='subtitle2'>{intl.formatMessage({ id: 'dt.header.script' })}</Typography>
              <FsDirentSelectSingle
                value={script}
                onChange={(value: string) => {
                  setCommands(addCommand({ type: 'SET_HEADER_SCRIPT', id: header.id, value }, commands));
                  setScript(value);
                }}
                options={(expressions ?? []).map((type) => ({ value: type, label: type }))}
              />
            </>
          )}
        </Stack>
      </DialogContent>
      <DialogActions>
        <FsDirentButtonDelete assetId={header.id} onDelete={() => { onChange([{ type: 'DELETE_HEADER', id: header.id }]); onClose(); }} />
        <FsDirentButtonCancel onClick={onClose} />
        <FsDirentButtonSave onClick={() => {
          onChange(commands);
          onClose();
        }} />
      </DialogActions>
    </Dialog>
  );
};

export type { HeaderEditProps };
export { HeaderEdit };
