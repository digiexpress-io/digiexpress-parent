import React from 'react'


import { TextareaAutosize } from '@mui/material';

import Burger from '@the-wrench-io/react-burger';
import { Client } from '../../context';


interface UploadCSVProps {
  onClose: () => void;
  onChange: (commands: Client.AstCommand[]) => void;
}

const UploadCSV: React.FC<UploadCSVProps> = ({ onChange, onClose }) => {
  const [csv, setCsv] = React.useState('');

  return (<Burger.Dialog title="decisions.toolbar.csvUpload" open={true} onClose={onClose}
    backgroundColor="uiElements.main"
    submit={{
      title: "buttons.apply",
      disabled: false,
      onClick: () => {
        const commands: Client.AstCommand[] = [];
        if (csv.trim().length > 0) {
          commands.push({ type: 'IMPORT_ORDERED_CSV', value: csv, id: '' });
        }
        if (commands.length > 0) {
          onChange(commands);
        }
        onClose();
      }
    }}>
    <TextareaAutosize minRows={10}
      style={{ width: '100%', height: '100%' }}
      value={csv}
      onChange={({ target }) => setCsv(target.value)}
    />
  </Burger.Dialog>);
}

export type { UploadCSVProps };
export { UploadCSV };
