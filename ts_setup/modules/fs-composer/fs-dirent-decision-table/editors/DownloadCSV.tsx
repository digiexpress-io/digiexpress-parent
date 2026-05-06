import React from 'react';
import { Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { Fs } from '@dxs-ts/fs-api';
import { FormattedMessage } from 'react-intl';

interface DownloadCSVProps {
  decision: Fs.DecisionAst;
  onClose: () => void;
}

const DownloadCSV: React.FC<DownloadCSVProps> = ({ onClose }) => {
  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='decisions.toolbar.csvDownload' /></DialogTitle>
      <DialogContent>
        {/* TODO */}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}><FormattedMessage id='button.cancel' /></Button>
        <Button onClick={onClose}><FormattedMessage id='buttons.download' /></Button>
      </DialogActions>
    </Dialog>
  );
};

export type { DownloadCSVProps };
export { DownloadCSV };
