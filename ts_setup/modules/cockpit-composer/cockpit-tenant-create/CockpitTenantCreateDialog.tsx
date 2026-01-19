import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, FormControl, generateUtilityClass, Grid2, InputLabel, MenuItem, Select, styled, TextField, Typography, Zoom } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { CockpitApi, useCockpitsBackend } from '@dxs-ts/cockpit-api';

export interface CockpitTenantCreateDialogProps {
  open: boolean;
  onClose: () => void;
  onSuccess?: () => void;
  cockpitId: string;
}

export const CockpitTenantCreateDialog: React.FC<CockpitTenantCreateDialogProps> = ({ open, onClose, onSuccess, cockpitId }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const backend = useCockpitsBackend();

  const [externalId, setExternalId] = React.useState('');
  const [tenantType, setTenantType] = React.useState<CockpitApi.CockpitTenantType>('WRENCH');
  const [tenantDescription, setTenantDescription] = React.useState('');
  const [isCreating, setIsCreating] = React.useState(false);

  function handleExternalIdChange(e: React.ChangeEvent<HTMLInputElement>) {
    setExternalId(e.target.value);
  }

  function handleTenantTypeChange(e: any) {
    setTenantType(e.target.value as CockpitApi.CockpitTenantType);
  }

  function handleTenantDescriptionChange(e: React.ChangeEvent<HTMLInputElement>) {
    setTenantDescription(e.target.value);
  }

  async function handleCreate() {
    if (!isFormValid) {
      return
    };

    setIsCreating(true);
    try {
      const command: CockpitApi.CreateCockpitTenantCommand = {
        externalId,
        tenantType,
        tenantDescription
      };

      await backend.persistence.createOneCockpitTenant(cockpitId, command);

      onSuccess?.();
      onClose();
      setExternalId('');
      setTenantType('WRENCH');
      setTenantDescription('');
    } catch (error) {
      console.error('Failed to create tenant:', error);
    } finally {
      setIsCreating(false);
    }
  }

  function handleClose() {
    if (isCreating) {
      return;
    }
    setExternalId('');
    setTenantType('WRENCH');
    setTenantDescription('');
    onClose();
  }

  const isFormValid = externalId.trim() && tenantDescription.trim();

  return (
    <StyledCockpitTenantCreateDialog className={classes.createDialog} open={open} onClose={handleClose} maxWidth='md' slots={{ transition: Zoom }}>
      <DialogTitle>
        {intl.formatMessage({ id: 'cockpit.tenantCreate.title' })}
      </DialogTitle>

      <DialogContent>
        <Grid2 container spacing={3}>
          <Grid2 size={{ md: 12, lg: 12, xl: 12 }}>
            <Typography variant="body1" gutterBottom>
              {intl.formatMessage({ id: 'cockpit.tenantCreate.description' })}
            </Typography>
          </Grid2>

          <Grid2 size={{ md: 12, lg: 12, xl: 12 }}>
            <StyledTextField
              required
              fullWidth
              value={externalId}
              onChange={handleExternalIdChange}
              label={intl.formatMessage({ id: 'cockpit.tenantCreate.externalId.label' })}
              placeholder={intl.formatMessage({ id: 'cockpit.tenantCreate.externalId.placeholder' })}
              disabled={isCreating}
            />
          </Grid2>

          <Grid2 size={{ md: 12, lg: 12, xl: 12 }}>
            <FormControl fullWidth required disabled={isCreating}>
              <InputLabel id="tenant-type-label">
                {intl.formatMessage({ id: 'cockpit.tenantCreate.tenantType.label' })}
              </InputLabel>
              <Select
                labelId="tenant-type-label"
                value={tenantType}
                onChange={handleTenantTypeChange}
                label={intl.formatMessage({ id: 'cockpit.tenantCreate.tenantType.label' })}
              >
                <MenuItem value={'WRENCH'}>
                  {intl.formatMessage({ id: 'cockpit.tenantCreate.tenantType.wrench' })}
                </MenuItem>
                <MenuItem value={'STENCIL'}>
                  {intl.formatMessage({ id: 'cockpit.tenantCreate.tenantType.stencil' })}
                </MenuItem>
              </Select>
            </FormControl>
          </Grid2>

          <Grid2 size={{ md: 12, lg: 12, xl: 12 }}>
            <StyledTextField
              required
              fullWidth
              multiline
              rows={3}
              value={tenantDescription}
              onChange={handleTenantDescriptionChange}
              label={intl.formatMessage({ id: 'cockpit.tenantCreate.description.label' })}
              placeholder={intl.formatMessage({ id: 'cockpit.tenantCreate.description.placeholder' })}
              disabled={isCreating}
            />
          </Grid2>
        </Grid2>
      </DialogContent>

      <DialogActions>
        <Button variant='outlined' onClick={handleClose} disabled={isCreating}>
          {intl.formatMessage({ id: 'button.cancel' })}
        </Button>
        <Button onClick={handleCreate} disabled={!isFormValid || isCreating}>
          {isCreating
            ? intl.formatMessage({ id: 'cockpit.tenantCreate.creating' })
            : intl.formatMessage({ id: 'cockpit.tenantCreate.create' })
          }
        </Button>
      </DialogActions>
    </StyledCockpitTenantCreateDialog>
  );
};

const MUI_NAME = 'CockpitTenantCreateDialog';
const StyledCockpitTenantCreateDialog = styled(Dialog, {
  name: MUI_NAME,
  slot: 'createDialog',
  overridesResolver: (_props, styles) => {
    return [
      styles.createDialog
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
    createDialog: ['createDialog'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};