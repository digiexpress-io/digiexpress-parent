import React from 'react'

import { ListItemText } from '@mui/material';

import Burger from 'components-burger';
import { Client } from '../../context';


const hitPolicyOptions = [
  { key: 'ALL', value: 'ALL', text: 'ALL' },
  { key: 'FIRST', value: 'FIRST', text: 'FIRST' }
];


const NameDescHitPolicyEdit: React.FC<{
  decision: Client.AstDecision;
  onClose: () => void;
  onChange: (commands: Client.AstCommand[]) => void;
}> = ({ onChange, decision, onClose }) => {
  const [name, setName] = React.useState(decision.name);
  const [desc, setDesc] = React.useState(decision.description);
  const [hitpolicy, setHitpolicy] = React.useState<string>(decision.hitPolicy);


  return (<Burger.Dialog title="decisions.toolbar.nameAndHitpolicy" open={true} onClose={onClose}
    submit={{
      title: "buttons.apply",
      disabled: false,
      onClick: () => {
        const commands: Client.AstCommand[] = [];
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
      }
    }}>
    <>
      <Burger.TextField label='decisions.name' value={name} onChange={setName} />
      <Burger.TextField label='decisions.desc' value={desc ? desc : ""} onChange={setDesc} />
      <Burger.Select label="decisions.hitpolicy" helperText="decisions.hitpolicy.helper"
        selected={hitpolicy}
        onChange={setHitpolicy}
        items={hitPolicyOptions.map((type) => ({
          id: type.value,
          value: (<ListItemText primary={type.text} />)
        }))} />
    </>
  </Burger.Dialog>);
}

export { NameDescHitPolicyEdit };
