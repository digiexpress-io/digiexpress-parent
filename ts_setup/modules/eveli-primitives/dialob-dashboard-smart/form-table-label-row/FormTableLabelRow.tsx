import React from 'react';
import { Box, Chip, IconButton } from '@mui/material';
import { DashboardItem, useDialobForms } from '@dxs-ts/eveli-api';
import { Cancel as CancelIcon } from '@mui/icons-material';
import { Add as AddIcon } from '@mui/icons-material';
import { DialogAddLabel } from '../dialog-add-label';


export const FormTableLabelRow: React.FC<{ value: DashboardItem }> = ({ value: form }) => {
  const { deleteFormLabel } = useDialobForms();
  const [open, setOpen] = React.useState(false);
  

  function handleDelete(labelToDelete: string) {
    deleteFormLabel({ form, labelToDelete });
  }

  function handleClose() {
    setOpen(false);
  }
    function handleOpen() {
    setOpen(true);
  }

  const labels = form.metadata.labels ?? [];

  return (
    <Box display='flex' flexWrap='wrap' gap={1} alignItems='center'>

      {open && <DialogAddLabel onClose={handleClose} source={form} />}
      {labels.map((label, index) => (
        <Chip
          key={index}
          label={label}
          onDelete={() => handleDelete(label)}
          deleteIcon={<CancelIcon />}
        />
      ))}

      <IconButton size='small' onClick={handleOpen}>
        <AddIcon fontSize='small'/>
      </IconButton>
    </Box>
  );
}