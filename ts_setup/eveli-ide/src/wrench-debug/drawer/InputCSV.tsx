import React from 'react'

import { TextareaAutosize, Box, Divider, Chip, Typography,  Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl'

import * as Burger from '@/eveli-styles';
import { HdesApi } from '@/api-wrench';
import { CancelButton } from '@/eveli-styles';


interface InputCSVProps {
  value: string;
  selected?: HdesApi.EntityId;
  onClose: () => void;
  onSelect: (csv: string) => void;
}

const InputCSV: React.FC<InputCSVProps> = ({ onSelect, onClose, value }) => {
  const [csv, setCsv] = React.useState(value);




  return (
  <Dialog open={true} onClose={onClose}>
    <DialogTitle><FormattedMessage id='debug.input.csvUpload' /></DialogTitle>
    <DialogContent>
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
    </DialogContent>
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={() => {
            onSelect(csv);
            onClose();
          }}>
          <FormattedMessage id='buttons.apply'/>
        </Button>
      </DialogActions>
    </Dialog>);
}

export type { InputCSVProps };
export { InputCSV };
