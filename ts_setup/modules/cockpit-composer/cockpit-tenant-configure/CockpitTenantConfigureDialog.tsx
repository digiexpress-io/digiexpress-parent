import React from 'react';
import {
  Autocomplete, Button, Dialog, DialogActions, DialogContent, DialogTitle, generateUtilityClass,
  Grid2, styled, TextField, Typography, Zoom, createFilterOptions, Box
} from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { useCockpitsBackend, useCockpit } from '@dxs-ts/cockpit-api';
import { useTenantOptions } from './useTenantOptions';



export interface CockpitTenantConfigureDialogProps {
  open: boolean;
  cockpitId: string;
  onClose: () => void;
}

export const CockpitTenantConfigureDialog: React.FC<CockpitTenantConfigureDialogProps> = ({ open, onClose, cockpitId }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const backend = useCockpitsBackend();
  const { tenants, activity, cockpitContainer, refresh } = useCockpit();

  const wrenchConfig = useTenantOptions({ selected: tenants.wrench, options: activity.availableTenants.wrench });
  const stencilConfig = useTenantOptions({ selected: tenants.stencil, options: activity.availableTenants.stencil });
  const isFormValid = wrenchConfig.isValid && stencilConfig.isValid;


  async function handleCreate() {
    if (!isFormValid) {
      return;
    }

    try {
      // Create wrench tenant if wrench config is provided
      await backend.persistence.createOneCockpitTenant(cockpitId, {
        tenantType: 'WRENCH',
        externalId: wrenchConfig.active.value!.externalId,
        tenantDescription: wrenchConfig.description.value ?? ''
      });

      // Create stencil tenant if stencil config is provided
      await backend.persistence.createOneCockpitTenant(cockpitId, {
        tenantType: 'STENCIL',
        externalId: stencilConfig.active.value!.externalId,
        tenantDescription: stencilConfig.description.value ?? ''
      });

      await refresh();
      onClose();
      wrenchConfig.onClose();
      stencilConfig.onClose();
    } catch (error) {
      console.error('Failed to create tenant:', error);
    }
  }

  function handleClose() {
    wrenchConfig.onClose();
    stencilConfig.onClose();
    onClose();
  }

  return (
    <StyledCockpitTenantConfigureDialog className={classes.createDialog} open={open} onClose={handleClose} maxWidth='md' slots={{ transition: Zoom }}>
      <DialogTitle>{intl.formatMessage({ id: 'cockpit.tenantCreate.title' })}{": "}{cockpitContainer.config.cockpitConfigName}</DialogTitle>

      <DialogContent>
        <Grid2 container spacing={3}>
          <Grid2 size={12}>
            <Typography>{intl.formatMessage({ id: 'cockpit.tenantCreate.description' })}</Typography>
          </Grid2>

          <Grid2 size={12}>
            <Typography variant='h6' fontWeight={600} mb={1}>
              {intl.formatMessage({ id: 'cockpit.wrenchConfig' })}
            </Typography>
            <StyledConfigBox>
              <Grid2 container spacing={2}>
                <Grid2 size={12}>
                  <Autocomplete selectOnFocus clearOnBlur freeSolo
                    value={wrenchConfig.active.value}
                    onChange={wrenchConfig.active.setValue}

                    options={wrenchConfig.options.values}
                    filterOptions={wrenchConfig.options.filter}
                    getOptionLabel={wrenchConfig.options.label}

                    renderOption={(props, option) => {
                      const { key, ...optionProps } = props;
                      return (
                        <li key={key} {...optionProps}>
                          {option.externalId}
                        </li>
                      );
                    }}
                    renderInput={(params) => (
                      <>
                        <Typography fontWeight={500}>{intl.formatMessage({ id: 'cockpit.tenantCreate.wrenchConfig.label' })}</Typography>
                        <TextField {...params} required fullWidth />
                      </>
                    )}
                  />
                </Grid2>
                <Grid2 size={12}>
                  <Typography fontWeight={500}>{intl.formatMessage({ id: 'cockpit.tenantDescription' })}</Typography>
                  <TextField required fullWidth value={wrenchConfig.description.value}
                    onChange={wrenchConfig.description.setValue}
                    placeholder={intl.formatMessage({ id: 'cockpit.tenantCreate.description.placeholder' })}
                  />
                </Grid2>
              </Grid2>
            </StyledConfigBox>
          </Grid2>

          <Grid2 size={12}>
            <Typography variant='h6' fontWeight={600} mb={1}>
              {intl.formatMessage({ id: 'cockpit.stencilConfig' })}
            </Typography>
            <StyledConfigBox>
              <Grid2 container spacing={2}>
                <Grid2 size={12}>
                  <Autocomplete selectOnFocus clearOnBlur freeSolo
                    value={stencilConfig.active.value}
                    onChange={stencilConfig.active.setValue}

                    options={stencilConfig.options.values}
                    filterOptions={stencilConfig.options.filter}
                    getOptionLabel={stencilConfig.options.label}

                    renderOption={(props, option) => {
                      const { key, ...optionProps } = props;
                      return (
                        <li key={key} {...optionProps}>
                          {option.externalId}
                        </li>
                      );
                    }}
                    renderInput={(params) => (
                      <>
                        <Typography fontWeight={500}>{intl.formatMessage({ id: 'cockpit.tenantCreate.stencilConfig.label' })}</Typography>
                        <TextField {...params} required fullWidth />
                      </>
                    )}
                  />
                </Grid2>
                <Grid2 size={12}>
                  <Typography fontWeight={500}>{intl.formatMessage({ id: 'cockpit.tenantDescription' })}</Typography>
                  <TextField required fullWidth value={stencilConfig.description.value}
                    onChange={stencilConfig.description.setValue}
                    placeholder={intl.formatMessage({ id: 'cockpit.tenantCreate.description.placeholder' })}
                  />
                </Grid2>
              </Grid2>
            </StyledConfigBox>
          </Grid2>
        </Grid2>
      </DialogContent>

      <DialogActions>
        <Button variant='outlined' onClick={handleClose}>
          {intl.formatMessage({ id: 'button.cancel' })}
        </Button>
        <Button onClick={handleCreate} disabled={!isFormValid}>{intl.formatMessage({ id: 'button.accept' })}
        </Button>
      </DialogActions>
    </StyledCockpitTenantConfigureDialog>
  );
};

const StyledConfigBox = styled(Box)(({ theme }) => ({
  border: '1px solid',
  borderColor: theme.palette.divider,
  padding: theme.spacing(2),
  marginTop: theme.spacing(1),
  backgroundColor: theme.palette.grey[50],
}));

const MUI_NAME = 'CockpitTenantConfigureDialog';
const StyledCockpitTenantConfigureDialog = styled(Dialog, {
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


const useUtilityClasses = () => {
  const slots = {
    createDialog: ['createDialog'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};