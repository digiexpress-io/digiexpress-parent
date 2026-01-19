import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Divider, generateUtilityClass, Grid2, styled, TextField, Typography, Zoom } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { useCockpit } from '../cockpit-provider';

export interface CockpitWrenchStencilEditDialogProps {
  open: boolean;
  onClose: () => void;
}

export const CockpitWrenchStencilEditDialog: React.FC<CockpitWrenchStencilEditDialogProps> = ({ open, onClose }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const { cockpitContainer } = useCockpit();
  const { config } = cockpitContainer;

  const [wrenchConfig, setWrenchConfig] = React.useState('');
  const [stencilConfig, setStencilConfig] = React.useState('');

  function handleWrenchConfigChange(e: React.ChangeEvent<HTMLInputElement>) {
    setWrenchConfig(e.target.value);
  }

  function handleStencilConfigChange(e: React.ChangeEvent<HTMLInputElement>) {
    setStencilConfig(e.target.value);
  }

  async function handleSave() {
    // TODO: Implement save logic for wrench and stencil configuration
    console.log('Saving wrench config:', wrenchConfig);
    console.log('Saving stencil config:', stencilConfig);
    onClose();
  }

  const isFormValid = wrenchConfig.trim() && stencilConfig.trim();

  return (
    <StyledCockpitWrenchStencilEditDialog className={classes.editDialog} open={open} onClose={onClose} maxWidth='md' slots={{ transition: Zoom }}>
      <DialogTitle>
        {intl.formatMessage({ id: 'cockpit.wrenchStencilEdit.title' })}{": "}{config.cockpitConfigName}
      </DialogTitle>

      <DialogContent>
        <Grid2 container display='flex' alignItems='center'>
          <Grid2 size={{ md: 12, lg: 12, xl: 12 }}>
            <Typography>{intl.formatMessage({ id: 'cockpit.wrenchStencilEdit.wrenchConfig.title' })}</Typography>
            <StyledTextField
              required
              fullWidth
              value={wrenchConfig}
              onChange={handleWrenchConfigChange}
              label={intl.formatMessage({ id: 'cockpit.wrenchStencilEdit.wrenchTools.label' })}
              placeholder={intl.formatMessage({ id: 'cockpit.wrenchStencilEdit.wrenchTools.placeholder' })}
            />
          </Grid2>

          <Grid2 size={{ md: 12, lg: 12, xl: 12 }}>
            <Divider sx={{ my: 3 }} />
            <Typography>{intl.formatMessage({ id: 'cockpit.wrenchStencilEdit.stencilConfig.title' })}</Typography>
            <StyledTextField
              required
              fullWidth
              value={stencilConfig}
              onChange={handleStencilConfigChange}
              label={intl.formatMessage({ id: 'cockpit.wrenchStencilEdit.stencilConfig.label' })}
              placeholder={intl.formatMessage({ id: 'cockpit.wrenchStencilEdit.stencilConfig.placeholder' })}
            />
          </Grid2>
        </Grid2>
      </DialogContent>

      <DialogActions>
        <Button variant='outlined' onClick={onClose}>
          {intl.formatMessage({ id: 'button.cancel' })}
        </Button>
        <Button onClick={handleSave} disabled={!isFormValid}>
          {intl.formatMessage({ id: 'button.save' })}
        </Button>
      </DialogActions>
    </StyledCockpitWrenchStencilEditDialog>
  );
};

const MUI_NAME = 'CockpitWrenchStencilEditDialog';
const StyledCockpitWrenchStencilEditDialog = styled(Dialog, {
  name: MUI_NAME,
  slot: 'editDialog',
  overridesResolver: (_props, styles) => {
    return [
      styles.editDialog
    ];
  },
})(({ theme }) => {
  return {};
});

const StyledTextField = styled(TextField)(({ theme }) => ({
  width: '100%',
  '& .MuiInputBase-input': {
    height: '2.5rem',
    padding: '0 12px'
  },
  '& .MuiInputBase-multiline': {
    paddingLeft: '0px',
    paddingRight: '0px'
  },
}));

const useUtilityClasses = () => {
  const slots = {
    editDialog: ['editDialog'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};