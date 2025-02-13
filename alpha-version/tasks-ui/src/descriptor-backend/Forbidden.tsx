import { Dialog, DialogTitle, DialogContent, DialogActions, Box, Stack, Divider, alpha, Theme, styled } from '@mui/material';
import { BackendAccess } from "./backend-types";
import Burger from 'components-burger';


export const Forbidden: React.FC<{ access: BackendAccess | undefined, onClose: () => void }> = ({ access, onClose }) => {
  if(!access) {
    return null;
  }

  return (<Dialog open={true} onClose={onClose} sx={{ m: 2 }}>
    <DialogTitle><p>FORBIDDEN 403!!!</p></DialogTitle>
    <DialogContent>
      <b>{access.message}</b>
    </DialogContent>
    <DialogActions>
      <Burger.SecondaryButton label='buttons.cancel' onClick={onClose} />
    </DialogActions>
  </Dialog>);
}
