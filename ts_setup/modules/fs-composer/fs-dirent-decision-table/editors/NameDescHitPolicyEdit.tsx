import React from 'react';
import { Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { Fs } from '@dxs-ts/fs-api';
import { FormattedMessage } from 'react-intl';

interface NameDescHitPolicyEditProps {
  decision: Fs.DecisionAst;
  onClose: () => void;
  onChange: (commands: Fs.AstCommand[]) => void;
}

const NameDescHitPolicyEdit: React.FC<NameDescHitPolicyEditProps> = ({ onClose }) => {
  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='decisions.toolbar.nameAndHitpolicy' /></DialogTitle>
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

export type { NameDescHitPolicyEditProps };
export { NameDescHitPolicyEdit };
