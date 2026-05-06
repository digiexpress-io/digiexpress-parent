import React from 'react';
import { Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { Fs } from '@dxs-ts/fs-api';
import { FormattedMessage } from 'react-intl';

interface OrderEditProps {
  decision: Fs.DecisionAst;
  onClose: () => void;
  onChange: (commands: Fs.AstCommand[]) => void;
}

const OrderEdit: React.FC<OrderEditProps> = ({ onClose }) => {
  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='decisions.toolbar.organize.rows.columns' /></DialogTitle>
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

export type { OrderEditProps };
export { OrderEdit };
