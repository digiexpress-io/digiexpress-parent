import { Button, Dialog, DialogActions, DialogContent, DialogTitle, generateUtilityClass, Grid2, styled, TextField, Typography, Zoom } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import React from 'react';


export interface EditDialogProps {
  dialogTitle: string;
  open: boolean,
  onClose: () => void
}

export const EditDialog: React.FC<EditDialogProps> = (props) => {
  const classes = useUtilityClasses();

  return (
    <StyledTaskEditDialog className={classes.editDialog} open={props.open} onClose={props.onClose} maxWidth='md' slots={{ transition: Zoom }}>
      <DialogTitle>{props.dialogTitle}</DialogTitle>
      <DialogContent>
        <Grid2 container spacing={1} display='flex' alignItems='center'>

          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight='bold'>Due date</Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <StyledTextField sx={{ width: '100%' }} value='06.11.2025' />
          </Grid2>

          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight='bold'>Customer name</Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <StyledTextField sx={{ width: '100%' }} value='John Smith' />
          </Grid2>

          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight='bold'>Subject</Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <StyledTextField sx={{ width: '100%' }} value='Send Feedback' />
          </Grid2>

          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight='bold'>Info</Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <StyledTextField sx={{ width: '100%' }} value='Tough customer, be sure to contact asap' />
          </Grid2>
        </Grid2>

      </DialogContent>
      <DialogActions>
        <Button variant='outlined' onClick={props.onClose}>Cancel</Button>
        <Button onClick={props.onClose}>Save</Button>
      </DialogActions>
    </StyledTaskEditDialog>
  )
}

const StyledTextField = styled(TextField)(({ theme }) => ({
  width: '100%',
  '& .MuiInputBase-input': {
    height: '2.5rem',
    boxSizing: 'border-box',
    padding: '0 12px'
  },
}));


const MUI_NAME = 'TaskEditDialog';
const StyledTaskEditDialog = styled(Dialog, {
  name: MUI_NAME,
  slot: 'EditDialog',
  overridesResolver: (_props, styles) => {
    return [
      styles.editDialog
    ];
  },

})(({ theme }) => {

  return {};
})


export const useUtilityClasses = () => {
  const slots = {
    editDialog: ['editDialog'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
