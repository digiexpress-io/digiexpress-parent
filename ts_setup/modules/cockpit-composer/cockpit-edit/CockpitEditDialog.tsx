import React from 'react';
import {
  Button, DialogActions, DialogContent, DialogTitle, Grid2, Typography, Zoom,
  FormControl, RadioGroup, FormControlLabel, Radio, Tooltip
} from '@mui/material';
import { useIntl } from 'react-intl';

import { useCockpit } from '@dxs-ts/cockpit-api';
import { useUtilityClasses, StyledCockpitEditDialog, StyledTextField, StyledEllipsisTypography } from './useUtilityClasses';

export interface CockpitEditDialogProps {
  open: boolean;
  onClose: () => void;
}

export const CockpitEditDialog: React.FC<CockpitEditDialogProps> = ({ open, onClose }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const { cockpitContainer, tenants, activity } = useCockpit();
  const { config } = cockpitContainer;

  const [isActive, setIsActive] = React.useState<string>(
    activity.activeCockpitId === config.id ? 'active' : 'inactive'
  );

  function handleActiveChange(event: React.ChangeEvent<HTMLInputElement>) {
    setIsActive(event.target.value);
  }

  function handleSave() {
    // TODO: Add save functionality
    onClose();
  }

  return (
    <StyledCockpitEditDialog
      className={classes.editDialog}
      open={open}
      onClose={onClose}
      maxWidth='md'
      slots={{ transition: Zoom }}
    >
      <DialogTitle>
        {intl.formatMessage({ id: 'cockpit.edit' })}{": "}{config.cockpitConfigName}
      </DialogTitle>

      <DialogContent>
        <Grid2 container display='flex' alignItems='center'>
          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight={500}> {intl.formatMessage({ id: 'cockpit.name' })}</Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <StyledTextField value={config.cockpitConfigName} disabled />
          </Grid2>

          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight={500}>{intl.formatMessage({ id: 'cockpit.description' })}</Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <StyledTextField value={config.cockpitConfigDesc} disabled />
          </Grid2>

          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight={500}>{intl.formatMessage({ id: 'cockpit.status' })}</Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <FormControl>
              <RadioGroup row value={isActive} onChange={handleActiveChange} >
                <FormControlLabel
                  value="active"
                  control={<Radio />}
                  label={intl.formatMessage({ id: 'cockpit.status.active' })}
                />
                <FormControlLabel
                  value="inactive"
                  control={<Radio />}
                  label={intl.formatMessage({ id: 'cockpit.status.inactive' })}
                />
              </RadioGroup>
            </FormControl>
          </Grid2>

          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight={500}>
              {intl.formatMessage({ id: 'cockpit.wrenchConfig' })}
            </Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <Grid2 container>
              <Grid2 size={6}>
                <Typography fontWeight={500}>{intl.formatMessage({ id: 'cockpit.config.name' })}</Typography>
                <Typography>{tenants.wrench?.externalId ?? '-'}</Typography>
              </Grid2>
              <Grid2 size={6}>
                <Typography fontWeight={500}>{intl.formatMessage({ id: 'cockpit.config.description' })}</Typography>
                {tenants.wrench?.cockpitConfigTenantDesc ? (
                  <Tooltip title={tenants.wrench.cockpitConfigTenantDesc} arrow>
                    <StyledEllipsisTypography>{tenants.wrench.cockpitConfigTenantDesc}</StyledEllipsisTypography>
                  </Tooltip>
                ) : (
                  <Typography>--</Typography>
                )}
              </Grid2>
            </Grid2>
          </Grid2>

          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight={500}>{intl.formatMessage({ id: 'cockpit.stencilConfig' })}</Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <Grid2 container>
              <Grid2 size={6}>
                <Typography fontWeight={500}>{intl.formatMessage({ id: 'cockpit.config.name' })}</Typography>
                <Typography>{tenants.stencil?.externalId ?? '-'}</Typography>
              </Grid2>
              <Grid2 size={6}>
                <Typography fontWeight={500}>
                  {intl.formatMessage({ id: 'cockpit.config.description' })}
                </Typography>
                {tenants.stencil?.cockpitConfigTenantDesc ? (
                  <Tooltip title={tenants.stencil.cockpitConfigTenantDesc} arrow>
                    <StyledEllipsisTypography>{tenants.stencil.cockpitConfigTenantDesc}</StyledEllipsisTypography>
                  </Tooltip>
                ) : (
                  <Typography>--</Typography>
                )}
              </Grid2>
            </Grid2>
          </Grid2>
        </Grid2>
      </DialogContent>

      <DialogActions>
        <Button variant='outlined' onClick={onClose}>
          {intl.formatMessage({ id: 'button.cancel' })}
        </Button>
        <Button onClick={handleSave}>
          {intl.formatMessage({ id: 'button.save' })}
        </Button>
      </DialogActions>
    </StyledCockpitEditDialog>
  );
};