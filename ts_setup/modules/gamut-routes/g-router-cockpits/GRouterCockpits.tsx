import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { useSite } from '@dxs-ts/gamut-api';
import { GCockpitDropdown } from './GCockpitDropdown';


export interface GRouterCockpitsProps {

}

export const GRouterCockpits: React.FC<GRouterCockpitsProps> = ({ }) => {
  const { cockpits } = useSite();
  const [showDialog, setShowDialog] = React.useState(false);
  const isEnabled = !!cockpits.active;


  // Keyboard shortcut
  React.useEffect(() => {
    if (!isEnabled) {
      return;
    }

    function handleKeyPress(e: KeyboardEvent) {
      if (e.ctrlKey && e.shiftKey && e.key === 'D') {
        e.preventDefault();
        setShowDialog(prev => !prev);
      }
    }

    window.addEventListener('keydown', handleKeyPress);
    return () => window.removeEventListener('keydown', handleKeyPress);
  }, [isEnabled]);


  function handleClose() {
    setShowDialog(false);
  }

  return (
    <Dialog open={showDialog} onClose={handleClose}>
      <DialogTitle variant='h2'>
        <FormattedMessage id='gamut.cockpit.selection.dialog' />
      </DialogTitle>
      <DialogContent>
        <GCockpitDropdown />
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose} autoFocus variant='contained'>
          <FormattedMessage id='gamut.buttons.close' />
        </Button>
      </DialogActions>
    </Dialog>);
}

