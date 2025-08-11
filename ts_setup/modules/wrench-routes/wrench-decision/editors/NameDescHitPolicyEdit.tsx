import React from 'react'

import { ListItemText, Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';

import * as Burger from '@dxs-ts/eveli-primitives';
import { HdesApi } from '@dxs-ts/wrench-api';
import { FormattedMessage } from 'react-intl';
import { CancelButton } from '@dxs-ts/eveli-primitives';

const hitPolicyOptions = [
  { key: 'ALL', value: 'ALL', text: 'ALL' },
  { key: 'FIRST', value: 'FIRST', text: 'FIRST' }
];


const NameDescHitPolicyEdit: React.FC<{
  decision:HdesApi.AstDecision;
  onClose: () => void;
  onChange: (commands:HdesApi.AstCommand[]) => void;
}> = ({ onChange, decision, onClose }) => {
  const [name, setName] = React.useState(decision.name);
  const [desc, setDesc] = React.useState(decision.description);
  const [hitpolicy, setHitpolicy] = React.useState<string>(decision.hitPolicy);

  return (
  <Dialog open={true} onClose={onClose}>
    <DialogTitle><FormattedMessage id='decisions.toolbar.nameAndHitpolicy' /></DialogTitle>
    <DialogContent>
      <Burger.TextField label='decisions.name' value={name} onChange={setName} />
      <Burger.TextField label='decisions.desc' value={desc ? desc : ""} onChange={setDesc} />
      <Burger.Select label="decisions.hitpolicy" helperText="decisions.hitpolicy.helper"
        selected={hitpolicy}
        onChange={setHitpolicy}
        items={hitPolicyOptions.map((type) => ({
          id: type.value,
          value: (<ListItemText primary={type.text} />)
        }))} />
    </DialogContent>
    <DialogActions>
      <CancelButton onClick={onClose} />
      <Button onClick={() => {
          const commands:HdesApi.AstCommand[] = [];
          if (name !== decision.name) {
            commands.push({ type: "SET_NAME", value: name, id: "" });
          }
          if (hitpolicy !== decision.hitPolicy) {
            commands.push({ type: "SET_HIT_POLICY", value: hitpolicy, id: "" });
          }
          if (desc !== decision.description) {
            commands.push({ type: "SET_DESCRIPTION", value: desc, id: "" });
          }
          if (commands.length > 0) {
            onChange(commands);
          }
          onClose();
        }}>
        <FormattedMessage id='buttons.apply'/>
      </Button>
    </DialogActions>
  </Dialog>);
}

export { NameDescHitPolicyEdit };
