import React from 'react'
import { TextareaAutosize, Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { HdesApi } from '@/burger';
import { FormattedMessage } from 'react-intl';


interface UploadCSVProps {
  onClose: () => void;
  onChange: (commands:HdesApi.AstCommand[]) => void;
}

const UploadCSV: React.FC<UploadCSVProps> = ({ onChange, onClose }) => {
  const [csv, setCsv] = React.useState('');
  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='decisions.toolbar.csvUpload' /></DialogTitle>
      <DialogContent>
        <TextareaAutosize minRows={10}
          style={{ width: '100%', height: '100%' }}
          value={csv}
          onChange={({ target }) => setCsv(target.value)}
        />
      </DialogContent>
      <DialogActions>
        <Button variant='text' onClick={onClose}>
          <FormattedMessage id='button.cancel'/>
        </Button>
        <Button onClick={() => {
            const commands:HdesApi.AstCommand[] = [];
            if (csv.trim().length > 0) {
              commands.push({ type: 'IMPORT_ORDERED_CSV', value: csv, id: '' });
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

export type { UploadCSVProps };
export { UploadCSV };
