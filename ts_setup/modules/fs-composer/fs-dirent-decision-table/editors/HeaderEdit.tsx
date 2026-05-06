import React from 'react';
import { Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { Fs } from '@dxs-ts/fs-api';
import { FormattedMessage } from 'react-intl';

interface HeaderEditProps {
  dt: Fs.DecisionAst;
  header: Fs.DecisionTypeDef;
  onClose: () => void;
  onChange: (commands: Fs.AstCommand[]) => void;
}

const HeaderEdit: React.FC<HeaderEditProps> = ({ onClose }) => {
  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='decisions.header.dialog.title.simple' /></DialogTitle>
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

export type { HeaderEditProps };
export { HeaderEdit };
