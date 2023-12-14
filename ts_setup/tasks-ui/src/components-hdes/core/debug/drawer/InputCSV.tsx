import React from 'react'

import { TextareaAutosize, Box, Divider, Chip, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl'

import Burger from 'components-burger';
import { Client } from '../../context';


interface InputCSVProps {
  value: string;
  selected?: Client.EntityId;
  onClose: () => void;
  onSelect: (csv: string) => void;
}

const InputCSV: React.FC<InputCSVProps> = ({ onSelect, onClose, value }) => {
  const [csv, setCsv] = React.useState(value);

  return (<Burger.Dialog title="debug.input.csvUpload" open={true} onClose={onClose}
    backgroundColor="uiElements.main"
    submit={{
      title: "buttons.apply",
      disabled: false,
      onClick: () => {
        onSelect(csv);
        onClose();
      }
    }}>

    <Box>
      <Box>
        <Box><Typography variant="h4" fontWeight="bold"><FormattedMessage id={"debug.input.csvFileTitle"} /></Typography></Box>
        <Burger.FileField value="" onChange={setCsv} label="debug.input.csvFile" />
      </Box>

      <Divider sx={{ mt: 4, mb: 4 }}>
        <Chip label={<FormattedMessage id={"debug.input.csvFileOrText"} />} />
      </Divider>

      <TextareaAutosize minRows={10}
        style={{ width: '100%', height: '100%' }}
        value={csv}
        onChange={({ target }) => setCsv(target.value)}
      />

    </Box>
  </Burger.Dialog>);
}

export type { InputCSVProps };
export { InputCSV };
