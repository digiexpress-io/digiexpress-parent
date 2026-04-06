import React from 'react';
import {
  Button, DialogActions, DialogContent, DialogTitle, Grid2, Typography, Zoom,
  FormControl, RadioGroup, FormControlLabel, Radio, Tooltip
} from '@mui/material';
import { useIntl } from 'react-intl';

import { useCockpit, useCockpitsBackend } from '@dxs-ts/cockpit-api';
import { useUtilityClasses, StyledCockpitEditDialog, StyledTextField } from './useUtilityClasses';


export interface CockpitEditDialogProps {
  open: boolean;
  onClose: () => void;
}

export const CockpitEditDialog: React.FC<CockpitEditDialogProps> = ({ open, onClose }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const { cockpitContainer, refresh } = useCockpit();

  const backend = useCockpitsBackend();

  const [isActive, setIsActive] = React.useState<string>(
    cockpitContainer.member?.aliasStatus ? 'active' : 'inactive'
  );

  function handleActiveChange(event: React.ChangeEvent<HTMLInputElement>) {
    setIsActive(event.target.value);
  }

  async function handleSave() {
    try {
      const aliasStatus = isActive === 'active';
      if(aliasStatus === cockpitContainer.member?.aliasStatus) {
        onClose();
        return;
      }

      const activeId = cockpitContainer.alias.id;
      await backend.persistence.changeActiveCockpit({ activeId });
      await refresh();
      onClose();
    } catch (error) {
      console.error('Error changing active cockpit:', error);
    }
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
        {intl.formatMessage({ id: 'cockpit.edit' })}{": "}{cockpitContainer.alias.aliasName}
      </DialogTitle>

      <DialogContent>
        <Grid2 container display='flex' alignItems='center'>
          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight={500}> {intl.formatMessage({ id: 'cockpit.name' })}</Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <StyledTextField value={cockpitContainer.alias.aliasName} disabled />
          </Grid2>

          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight={500}>{intl.formatMessage({ id: 'cockpit.description' })}</Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <StyledTextField value={cockpitContainer.alias.aliasDesc} disabled />
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

