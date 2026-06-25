import React from 'react';
import { Divider, TextField } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsSearchProps } from './FsSearchProps';
import { useOwnerState } from './useOwnerState';
import { FsSearchRoot, useUtilityClasses } from './useUtilityClasses';
import { FsDirentSelectMulti } from '../fs-utilities';

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

          <FsDirentSelectMulti
            options={ownerState.allAvailableTypeFilters.map(f =>
              ({ value: f.value, label: intl.formatMessage({ id: `fs.dirent.type.${f.value.toLowerCase()}` }) })
            )}
            value={ownerState.visibleFilters.filter(f => f.type === 'asset').map(f => f.value)}
            onChange={ownerState.handleTypeFilterSelectChange}
            placeholder={intl.formatMessage({ id: 'fs.search.filterSelect.placeholder' })}
            onClearAll={() => ownerState.handleTypeFilterSelectChange([])}
          />

          <FsDirentSelectMulti
            options={ownerState.availableLabelOptions.map(l => ({ value: l, label: l }))}
            value={ownerState.visibleFilters.filter(f => f.type === 'label').map(f => f.value)}
            onChange={ownerState.handleLabelFilterSelectChange}
            placeholder={intl.formatMessage({ id: 'fs.search.labelFilter.placeholder' })}
            onClearAll={() => ownerState.handleLabelFilterSelectChange([])}
          />

        </div>
      </FsSearchRoot>
      <Divider />
    </>
  );
};

