import React from 'react';
import {
  Autocomplete, Button, Dialog, DialogActions, DialogContent, DialogTitle, generateUtilityClass,
  Grid2, styled, TextField, Typography, Zoom, createFilterOptions
} from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { useCockpitsBackend, useCockpit } from '@dxs-ts/cockpit-api';
import { useTenantOptions } from './useTenantOptions';



export interface CockpitTenantEditDialog {
  open: boolean;
  cockpitId: string;
  onClose: () => void;
}

export const CockpitTenantEditDialog: React.FC<CockpitTenantEditDialog> = ({ open, onClose, cockpitId }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const backend = useCockpitsBackend();
  const { tenants, activity } = useCockpit();

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
    <StyledCockpitTenantEditDialog className={classes.createDialog} open={open} onClose={handleClose} maxWidth='md' slots={{ transition: Zoom }}>
      <DialogTitle>{intl.formatMessage({ id: 'cockpit.tenantCreate.title' })}</DialogTitle>

      <DialogContent>
        <Grid2 container spacing={1}>
          <Grid2 size={{ md: 12, lg: 12, xl: 12 }}>
            <Typography variant="body1">{intl.formatMessage({ id: 'cockpit.tenantCreate.description' })}</Typography>
          </Grid2>

          <Grid2 size={{ md: 12, lg: 12, xl: 12 }}>
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
          <Grid2 size={{ md: 12, lg: 12, xl: 12 }}>
            <Typography fontWeight={500}>{intl.formatMessage({ id: 'cockpit.tenantDescription' })}</Typography>
            <TextField required fullWidth value={wrenchConfig.description.value}
              onChange={wrenchConfig.description.setValue}
              placeholder={intl.formatMessage({ id: 'cockpit.tenantCreate.description.placeholder' })}
            />
          </Grid2>

          <Grid2 size={{ md: 12, lg: 12, xl: 12 }}>
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

          <Grid2 size={{ md: 12, lg: 12, xl: 12 }}>
            <Typography fontWeight={500}>{intl.formatMessage({ id: 'cockpit.tenantDescription' })}</Typography>
            <TextField required fullWidth value={stencilConfig.description.value}
              onChange={stencilConfig.description.setValue}
              placeholder={intl.formatMessage({ id: 'cockpit.tenantCreate.description.placeholder' })}
            />
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
    </StyledCockpitTenantEditDialog>
  );
};

const MUI_NAME = 'CockpitTenantEditDialog';
const StyledCockpitTenantEditDialog = styled(Dialog, {
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