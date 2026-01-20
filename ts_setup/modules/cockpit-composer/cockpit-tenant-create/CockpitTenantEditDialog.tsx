import React from 'react';
import {
  Autocomplete, Button, Dialog, DialogActions, DialogContent, DialogTitle, generateUtilityClass,
  Grid2, styled, TextField, Typography, Zoom, createFilterOptions
} from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { CockpitApi, useCockpitsBackend, useCockpit } from '@dxs-ts/cockpit-api';

interface ConfigOptionType {
  inputValue?: string;
  title: string;
}

export interface CockpitTenantEditDialog {
  open: boolean;
  onClose: () => void;
  cockpitId: string;
}

const filter = createFilterOptions<ConfigOptionType>();

export const CockpitTenantEditDialog: React.FC<CockpitTenantEditDialog> = ({ open, onClose, cockpitId }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const backend = useCockpitsBackend();
  const { cockpitContainer, activity } = useCockpit();

  const wrenchConfigOptions: ConfigOptionType[] = activity.availableTenants.wrench.map(tenant => ({ title: tenant.externalId }));
  const stencilConfigOptions: ConfigOptionType[] = activity.availableTenants.stencil.map(tenant => ({ title: tenant.externalId }));

  const [externalId, setExternalId] = React.useState('');
  const [wrenchConfig, setWrenchConfig] = React.useState<ConfigOptionType | undefined>(
    wrenchConfigOptions.length > 0 ? wrenchConfigOptions[0] : undefined
  );
  const [stencilConfig, setStencilConfig] = React.useState<ConfigOptionType | undefined>(
    stencilConfigOptions.length > 0 ? stencilConfigOptions[0] : undefined
  );
  const [tenantDescription, setTenantDescription] = React.useState('');


  function handleTenantDescriptionChange(e: React.ChangeEvent<HTMLInputElement>) {
    setTenantDescription(e.target.value);
  }

  async function handleCreate() {
    if (!isFormValid) {
      return;
    }

    try {
      // Create wrench tenant if wrench config is provided
      if (wrenchConfig?.title) {
        const wrenchCommand: CockpitApi.CreateCockpitTenantCommand = {
          externalId: wrenchConfig.title,
          tenantType: 'WRENCH',
          tenantDescription
        };
        await backend.persistence.createOneCockpitTenant(cockpitId, wrenchCommand);
      }

      // Create stencil tenant if stencil config is provided
      if (stencilConfig?.title) {
        const stencilCommand: CockpitApi.CreateCockpitTenantCommand = {
          externalId: stencilConfig.title,
          tenantType: 'STENCIL',
          tenantDescription
        };
        await backend.persistence.createOneCockpitTenant(cockpitId, stencilCommand);
      }
      onClose();
      setExternalId('');
      setWrenchConfig(undefined);
      setStencilConfig(undefined);
      setTenantDescription('');
    } catch (error) {
      console.error('Failed to create tenant:', error);
    }
  }

  function handleClose() {
    setExternalId('');
    setWrenchConfig(undefined);
    setStencilConfig(undefined);
    setTenantDescription('');
    onClose();
  }

  const isFormValid = tenantDescription.trim() && (wrenchConfig?.title || stencilConfig?.title);

  return (
    <StyledCockpitTenantEditDialog className={classes.createDialog} open={open} onClose={handleClose} maxWidth='md' slots={{ transition: Zoom }}>
      <DialogTitle>{intl.formatMessage({ id: 'cockpit.tenantCreate.title' })}</DialogTitle>

      <DialogContent>
        <Grid2 container spacing={1}>
          <Grid2 size={{ md: 12, lg: 12, xl: 12 }}>
            <Typography variant="body1">{intl.formatMessage({ id: 'cockpit.tenantCreate.description' })}</Typography>
          </Grid2>

          <Grid2 size={{ md: 12, lg: 12, xl: 12 }}>
            <Autocomplete
              value={wrenchConfig}
              onChange={(event, newValue) => {
                if (typeof newValue === 'string') {
                  setWrenchConfig({
                    title: newValue,
                  });
                } else if (newValue && newValue.inputValue) {
                  setWrenchConfig({
                    title: newValue.inputValue,
                  });
                } else {
                  setWrenchConfig(newValue || undefined);
                }
              }}
              filterOptions={(options, params) => {
                const filtered = filter(options, params);
                const { inputValue } = params;
                const isExisting = options.some((option) => inputValue === option.title);
                if (inputValue !== '' && !isExisting) {
                  filtered.push({
                    inputValue,
                    title: `Add "${inputValue}"`,
                  });
                }
                return filtered;
              }}
              selectOnFocus
              clearOnBlur
              options={wrenchConfigOptions}
              getOptionLabel={(option) => {
                if (typeof option === 'string') {
                  return option;
                }
                if (option.inputValue) {
                  return option.inputValue;
                }
                return option.title;
              }}
              renderOption={(props, option) => {
                const { key, ...optionProps } = props;
                return (
                  <li key={key} {...optionProps}>
                    {option.title}
                  </li>
                );
              }}
              freeSolo
              renderInput={(params) => (
                <>
                  <Typography fontWeight={500}>{intl.formatMessage({ id: 'cockpit.tenantCreate.wrenchConfig.label' })}</Typography>
                  <TextField {...params} required fullWidth />
                </>
              )}
            />
          </Grid2>

          <Grid2 size={{ md: 12, lg: 12, xl: 12 }}>
            <Autocomplete
              value={stencilConfig}
              onChange={(event, newValue) => {
                if (typeof newValue === 'string') {
                  setStencilConfig({
                    title: newValue,
                  });
                } else if (newValue && newValue.inputValue) {
                  setStencilConfig({
                    title: newValue.inputValue,
                  });
                } else {
                  setStencilConfig(newValue || undefined);
                }
              }}
              filterOptions={(options, params) => {
                const filtered = filter(options, params);
                const { inputValue } = params;
                const isExisting = options.some((option) => inputValue === option.title);
                if (inputValue !== '' && !isExisting) {
                  filtered.push({
                    inputValue,
                    title: `Add "${inputValue}"`,
                  });
                }
                return filtered;
              }}
              selectOnFocus
              clearOnBlur
              options={stencilConfigOptions}
              getOptionLabel={(option) => {
                if (typeof option === 'string') {
                  return option;
                }
                if (option.inputValue) {
                  return option.inputValue;
                }
                return option.title;
              }}
              renderOption={(props, option) => {
                const { key, ...optionProps } = props;
                return (
                  <li key={key} {...optionProps}>
                    {option.title}
                  </li>
                );
              }}
              freeSolo
              renderInput={(params) => (
                <>
                  <Typography fontWeight={500}>{intl.formatMessage({ id: 'cockpit.tenantCreate.stencilConfig.label' })}</Typography>
                  <TextField {...params} required fullWidth />
                </>
              )}
            />
          </Grid2>

          <Grid2 size={{ md: 12, lg: 12, xl: 12 }}>
            <Typography fontWeight={500}>{intl.formatMessage({ id: 'cockpit.tenantCreate.tenantDescription.label' })}</Typography>
            <TextField required fullWidth value={tenantDescription}
              onChange={handleTenantDescriptionChange}
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