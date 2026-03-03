import React from 'react';
import { TextField, Typography, FormControl, Select, MenuItem, Chip, OutlinedInput } from '@mui/material';
import { FsIcons } from '../fs-theme';
import { FsDirentCreateProps, configOptions, mockSelectedValues } from './FsDirentCreateProps';
import { useUtilityClasses, FsDirentCreateRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

export const FsDirentCreate: React.FC<FsDirentCreateProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentCreateRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.caption}>Create new item</Typography>
      <div className={classes.formContainer}>
        <Typography className={classes.label}>Name</Typography>
        <TextField
          className={classes.textField}
          placeholder='Asset name'
          size='small'
          fullWidth
        />

        <Typography className={classes.label}>3-digit order number</Typography>
        <TextField
          className={classes.textField}
          placeholder="100"
          size='small'
          fullWidth
        />

        <Typography className={classes.label}>Description</Typography>
        <TextField
          className={classes.textField}
          placeholder="Description"
          size='small'
          fullWidth
          multiline
          minRows={2}
          maxRows={5}
        />

        <Typography className={classes.label}>Config options</Typography>
        <FormControl className={classes.formControl} fullWidth size='small'>
          <Select
            className={classes.select}
            multiple
            value={mockSelectedValues}
            input={<OutlinedInput />}
            renderValue={(selected) => (
              <div className={classes.chipContainer}>
                {(selected as string[]).map((value) => {
                  const option = configOptions.find(opt => opt.value === value);
                  const IconComponent = FsIcons[option?.icon as keyof typeof FsIcons];
                  return (
                    <Chip
                      key={value}
                      className={classes.chip}
                      icon={IconComponent ? <IconComponent fontSize='small' /> : undefined}
                      label={option?.label}
                      size="small"
                    />
                  );
                })}
              </div>
            )}
          >
            {configOptions.map((option) => {
              const IconComponent = FsIcons[option.icon as keyof typeof FsIcons];
              return (
                <MenuItem key={option.value} value={option.value} className={classes.menuItem}>
                  <div className={classes.menuItemContent}>
                    {IconComponent && <IconComponent fontSize='small' />}
                    {option.label}
                  </div>
                </MenuItem>
              );
            })}
          </Select>
        </FormControl>

        <Typography className={classes.label}>Labels</Typography>
        <TextField
          className={classes.textField}
          size='small'
          fullWidth
          placeholder="Select or add new labels"
        />

        <Typography className={classes.label}>Comments</Typography>
        <TextField
          className={classes.textField}
          placeholder="Notes about this asset"
          size='small'
          fullWidth
          multiline
          minRows={2}
          maxRows={5}
        />

        <Typography className={classes.sectionTitle}>Sharing and Permissions</Typography>
        <div className={classes.sectionBox}>
          <Typography className={classes.sectionContent}>Put content here</Typography>
        </div>

        <div className={classes.buttonContainer}>
          <button className={classes.cancelButton}>Cancel</button>
          <button className={classes.saveButton}>Save</button>
        </div>
      </div>
    </FsDirentCreateRoot>
  );
};