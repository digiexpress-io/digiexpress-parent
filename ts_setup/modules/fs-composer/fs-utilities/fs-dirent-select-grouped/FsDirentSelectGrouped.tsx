import React from 'react';
import {
  DialogTitle, DialogContent, DialogActions, Chip, Checkbox,
  Typography, FormControlLabel, Stack, Divider, IconButton, Box, Button
} from '@mui/material';
import { useIntl } from 'react-intl';
import { FsIcons } from '../../fs-theme';
import { FsDirentSelectGroupedProps } from './FsDirentSelectGroupedProps';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentSelectGroupedRoot, useUtilityClasses } from './useUtilityClasses';

export const FsDirentSelectGrouped: React.FC<FsDirentSelectGroupedProps> = ({ open, onClose, title, value, onChange, groups }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const [activeLocales, setActiveLocales] = React.useState<string[]>(() => groups.map(g => g.localeId));
  const [search, setSearch] = React.useState('');

  React.useEffect(() => {
    setActiveLocales(groups.map(g => g.localeId));
  }, [groups.length]);

  function toggleLocale(localeId: string) {
    if (activeLocales.includes(localeId)) {
      setActiveLocales(prev => prev.filter(l => l !== localeId));
    } else {
      setActiveLocales(prev => [...prev, localeId]);
    }
  }

  function toggleItem(id: string) {
    if (value.includes(id)) {
      onChange(value.filter(v => v !== id));
    } else {
      onChange([...value, id]);
    }
  }

  const searchString = search.toLowerCase();

  const visibleGroups = groups
    .filter(g => activeLocales.includes(g.localeId))
    .map(g => ({
      ...g,
      items: g.items.filter(item =>
        !value.includes(item.id) &&
        (searchString === '' || item.label.toLowerCase().includes(searchString))
      ),
    }));

  const selectedGroups = groups
    .map(g => ({ ...g, items: g.items.filter(item => value.includes(item.id)) }))
    .filter(g => g.items.length > 0);

  const isSelected = selectedGroups.length > 0;

  return (
    <FsDirentSelectGroupedRoot open={open} onClose={onClose} fullWidth maxWidth="md">
      <DialogTitle component="div" className={classes.dialogTitle}>
        <Box className={classes.titleRow}>
          <Typography variant="h1">{title ?? intl.formatMessage({ id: 'button.select' })}</Typography>
          <IconButton size="small" onClick={onClose}><FsIcons.Close fontSize="small" /></IconButton>
        </Box>
        <Stack direction="column" spacing={1}>
          <Stack direction="row" spacing={1} flexWrap="wrap" alignItems='center'>
            <Typography>{intl.formatMessage({ id: 'fs.direntSelectGrouped.localeFilter.label' })}</Typography>

            {groups.map(g => (
              <Chip key={g.localeId} label={g.localeLabel} size="medium"
                variant={activeLocales.includes(g.localeId) ? 'filled' : 'outlined'}
                onClick={() => toggleLocale(g.localeId)}
              />
            ))}
          </Stack>
          <Box className={classes.searchField}>
            <FsDirentTextField value={search}
              onChange={(e) => setSearch(e)}
              placeholder={intl.formatMessage({ id: 'search.field.placeholder' })}
            />
          </Box>
        </Stack>
      </DialogTitle>

      <DialogContent>
        {isSelected && (
          <>
            <Typography variant="subtitle1" className={classes.selectedLabel}>
              {intl.formatMessage({ id: 'fs.direntSelectGrouped.associatedItems.label' })}
            </Typography>
            <Box className={classes.selectedBox}>
              {selectedGroups.map((group, index) => (
                <React.Fragment key={group.localeId}>
                  {index > 0 && <Divider className={classes.groupDivider} />}
                  <Typography variant="subtitle2" className={classes.groupLabel}>
                    {group.localeLabel}
                  </Typography>
                  {group.items.map(item => (
                    <FormControlLabel key={item.id} label={<Typography variant="body2">{item.label}</Typography>}
                      control={<Checkbox size="small" checked onChange={() => toggleItem(item.id)} />}
                      className={classes.formControlLabel}
                    />
                  ))}
                </React.Fragment>
              ))}
            </Box>
            <Divider className={classes.sectionDivider} />
          </>
        )}

        {visibleGroups.map((group, index) => (
          <React.Fragment key={group.localeId}>
            {index > 0 && <Divider className={classes.groupDivider} />}
            <Typography variant="subtitle2" className={classes.groupLabel}>
              {group.localeLabel}
            </Typography>
            {group.items.length === 0 ? <Typography variant="body2" color="text.secondary">--</Typography> : group.items.map(item => (
              <FormControlLabel
                key={item.id}
                label={<Typography variant="body2">{item.label}</Typography>}
                control={
                  <Checkbox size="small" checked={value.includes(item.id)} onChange={() => toggleItem(item.id)} />
                }
                className={classes.formControlLabel}
              />
            ))
            }
          </React.Fragment>
        ))}
      </DialogContent>
      <DialogActions>
        <Button variant="contained" onClick={onClose}>{intl.formatMessage({ id: 'button.accept' })}</Button>
      </DialogActions>
    </FsDirentSelectGroupedRoot>
  );
};
