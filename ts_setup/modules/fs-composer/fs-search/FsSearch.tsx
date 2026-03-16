import React from 'react';
import { Divider, TextField, Select, MenuItem, OutlinedInput } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsColors } from '../fs-theme';
import { FsSearchProps } from './FsSearchProps';
import { useOwnerState } from './useOwnerState';
import { FsSearchRoot, useUtilityClasses } from './useUtilityClasses';
import { FsFilterChip } from './FsFilterChip';

export const FsSearch: React.FC<FsSearchProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses(props);

  if (!ownerState.open) {
    return null;
  }

  return (
    <>
      <FsSearchRoot ownerState={ownerState} className={classes.root}>
        <div className={classes.container}>
          <TextField className={classes.searchField}
            placeholder={intl.formatMessage({ id: 'fs.search.searchField.placeholder' })}
            fullWidth
            value={ownerState.searchTerm}
            onChange={ownerState.handleSearchChange}
            type="search"
          />

          <Select className={classes.multiSelect}
            multiple
            value={ownerState.visibleFilters.map(f => f.label)}
            onChange={(e) => ownerState.handleFilterSelectChange(e.target.value as string[])}
            displayEmpty
            input={<OutlinedInput />}
            {...(ownerState.isDarkMode && {
              MenuProps: {
                PaperProps: {
                  sx: {
                    backgroundColor: FsColors.dark.surface,
                    color: FsColors.dark.text,
                    borderWidth: '1px',
                    borderTop: 'unset',
                    '& .MuiMenuItem-root': {
                      backgroundColor: FsColors.dark.surface,
                    },
                    '& .MuiMenuItem-root:hover': {
                      backgroundColor: FsColors.dark.background,
                    },
                    '& .Mui-selected': {
                      backgroundColor: FsColors.dark.text,
                      color: FsColors.dark.background
                    },
                    '& .Mui-selected:hover': {
                      backgroundColor: FsColors.dark.surface,
                      color: FsColors.dark.text
                    },
                  },
                },
              }
            })}
            renderValue={(selected) => {
              if ((selected as string[]).length === 0) {
                return <span className={classes.placeholderText}>{intl.formatMessage({ id: 'fs.search.filterSelect.placeholder' })}</span>;
              }
              return (
                <div className={classes.chipContainer}>
                  {(selected as string[]).map((label) => {
                    const filter = ownerState.allAvailableFilters.find(f => f.label === label);
                    return (
                      <FsFilterChip
                        key={label}
                        label={label}
                        chipType={filter?.type || 'folder'}
                        isDarkMode={ownerState.isDarkMode}
                      />
                    );
                  })}
                </div>
              );
            }}
          >
            {ownerState.allAvailableFilters.map((filter) => (
              <MenuItem key={filter.type} value={filter.label}>
                {filter.label}
              </MenuItem>
            ))}
          </Select>
        </div>
      </FsSearchRoot>
      <Divider />
    </>
  );
};

