import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Stack, Typography } from '@mui/material';
import { FsDirentButtonCancel } from '../../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../../fs-dirent-button-save';
import { useIntl } from 'react-intl';
import { Fs } from '@dxs-ts/fs-api';
import { FsDirentTextField } from '../../fs-dirent-text-field';
import { FsDirentSelectSingle } from '../../fs-dirent-select-single';

const hitPolicyOptions = [
  { value: 'ALL', label: 'ALL' },
  { value: 'FIRST', label: 'FIRST' },
];

const NameDescHitPolicyEdit: React.FC<{
  decision: Fs.AstDecision;
  onClose: () => void;
  onChange: (commands: Fs.AstCommand[]) => void;
}> = ({ onChange, decision, onClose }) => {
  const intl = useIntl();
  const [name, setName] = React.useState(decision.name);
  const [desc, setDesc] = React.useState(decision.description ?? '');
  const [hitPolicy, setHitPolicy] = React.useState<string>(decision.hitPolicy);

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle>{intl.formatMessage({ id: 'decisions.toolbar.nameAndHitpolicy' })}</DialogTitle>
      <DialogContent>
        <Stack direction='column' gap={1}>
          <Typography variant='subtitle2'>{intl.formatMessage({ id: 'fs.dirent.decision_table.nameAndHitpolicy.name' })}</Typography>
          <FsDirentTextField placeholder='decisions.name' value={name} onChange={setName} />

          <Typography variant='subtitle2'>{intl.formatMessage({ id: 'fs.dirent.decision_table.nameAndHitpolicy.description' })}</Typography>
          <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.decision_table.nameAndHitpolicy.description.placeholder' })} value={desc} onChange={setDesc} />

          <Typography variant='subtitle2'>{intl.formatMessage({ id: 'fs.dirent.decision_table.nameAndHitpolicy.hitPolicy' })}</Typography>
          <FsDirentSelectSingle value={hitPolicy} onChange={setHitPolicy} options={hitPolicyOptions} />
          <Typography variant='caption' color='text.secondary'>{intl.formatMessage({ id: 'fs.dirent.decision_table.nameAndHitpolicy.hitPolicy.helper' })}</Typography>
        </Stack>
      </DialogContent>
      <DialogActions>
        <FsDirentButtonCancel onClick={onClose} />
        <FsDirentButtonSave onClick={() => {
          const commands: Fs.AstCommand[] = [];
          if (name !== decision.name) {
            commands.push({ type: 'SET_NAME', value: name, id: '' });
          }
          if (hitPolicy !== decision.hitPolicy) {
            commands.push({ type: 'SET_HIT_POLICY', value: hitPolicy, id: '' });
          }
          if (desc !== (decision.description ?? '')) {
            commands.push({ type: 'SET_DESCRIPTION', value: desc, id: '' });
          }
          if (commands.length > 0) {
            onChange(commands);
          }
          onClose();
        }} />
      </DialogActions>
      </Dialog >
    );
  };

export { NameDescHitPolicyEdit };
