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
  const { cockpitContainer, refresh } = useCockpit();

  const tenantOptions = useTenantOptions();
  const isFormValid = tenantOptions.isValid;


  async function handleCreate() {
    if (!isFormValid) {
      return;
    }

    try {
      // Create wrench tenant if wrench config is provided
      await backend.persistence.createOneCockpitTenant(cockpitId, {
        externalId: tenantOptions.active.value!.externalId,
        tenantDescription: tenantOptions.description.value ?? ''
      });

      await refresh();
      onClose();
      tenantOptions.onClose();
    } catch (error) {
      console.error('Failed to create tenant:', error);
    }
  }

  function handleClose() {
    tenantOptions.onClose();
    onClose();
  }

  return (
    <StyledCockpitTenantConfigureDialog className={classes.createDialog} open={open} onClose={handleClose} maxWidth='md' slots={{ transition: Zoom }}>
      <DialogTitle>{intl.formatMessage({ id: 'cockpit.tenantCreate.title' })}{": "}{cockpitContainer.alias.aliasName}</DialogTitle>

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
                    value={tenantOptions.active.value}
                    onChange={tenantOptions.active.setValue}

                    options={tenantOptions.options.values}
                    filterOptions={tenantOptions.options.filter}
                    getOptionLabel={tenantOptions.options.label}

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
                  <TextField required fullWidth value={tenantOptions.description.value}
                    onChange={tenantOptions.description.setValue}
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
                    value={tenantOptions.active.value}
                    onChange={tenantOptions.active.setValue}

                    options={tenantOptions.options.values}
                    filterOptions={tenantOptions.options.filter}
                    getOptionLabel={tenantOptions.options.label}

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
                  <TextField required fullWidth value={tenantOptions.description.value}
                    onChange={tenantOptions.description.setValue}
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