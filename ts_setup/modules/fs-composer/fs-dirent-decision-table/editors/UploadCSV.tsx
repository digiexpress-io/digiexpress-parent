import React from 'react';
import { Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { Fs } from '@dxs-ts/fs-api';
import { FormattedMessage } from 'react-intl';

interface UploadCSVProps {
  onClose: () => void;
  onChange: (commands: Fs.AstCommand[]) => void;
}

const UploadCSV: React.FC<UploadCSVProps> = ({ onClose }) => {
  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='decisions.toolbar.csvUpload' /></DialogTitle>
      <DialogContent>
        {/* TODO */}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}><FormattedMessage id='button.cancel' /></Button>
        <Button onClick={onClose}><FormattedMessage id='buttons.apply' /></Button>
      </DialogActions>
    </Dialog>
  );
};

export type { UploadCSVProps };
export { UploadCSV };
