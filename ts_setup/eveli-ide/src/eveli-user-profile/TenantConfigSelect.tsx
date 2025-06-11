import React from 'react';
import { Box, Select, FormControl, SelectChangeEvent, MenuItem, InputLabel, Chip, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { PrefsApi } from '@/api-prefs';
import { EveliFeatureMapping } from '@/api-tenant-config';
import { useFetch } from '@dxs-ts/eveli-fetch';




export interface TenantConfigSelectProps {
  userProfile: PrefsApi.UserProfile,
  onChange: (features: string[]) => void
}


export const TenantConfigSelect: React.FC<TenantConfigSelectProps> = ({ userProfile, onChange }) => {
  const intl = useIntl();
  // const { restApi } = useFetch('worker/rest/api/userprofiles/$profileId.GET', {})

  const [selectedFeatures, setSelectedFeatures] = React.useState<string[]>(userProfile.tenantFeatures ?? []);
  const tenantFeatureKeys = Object.keys(EveliFeatureMapping);
  const inputLabel = intl.formatMessage({ id: 'eveli.userProfile.tenantConfig.select', defaultMessage: 'Config option' });

  function handleChange(event: SelectChangeEvent<string[]>) {
    const { value } = event.target;
    const newSelected = typeof value === 'string' ? value.split(',') : value;
    setSelectedFeatures(newSelected);
    onChange(newSelected);
  };

  function handleDelete(e: React.MouseEvent<HTMLDivElement>, value: string) {
    e.stopPropagation();
    const updated = selectedFeatures.filter((item) => item !== value);
    setSelectedFeatures(updated);
    onChange(updated);
  }


  return (
    <Box sx={{ minWidth: 120 }}>
      <Typography>{intl.formatMessage({
        id: `eveli.userProfile.tenantConfig.select.title`,
        defaultMessage: 'Personalise your user experience by adding and removing features'
      })}
      </Typography>
      <FormControl fullWidth>
        <InputLabel>{inputLabel}</InputLabel>
        <Select multiple value={selectedFeatures} label={inputLabel} onChange={handleChange} renderValue={(selected) => (
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
            {(selected as string[]).map((value) => (
              <Chip key={value}
                label={intl.formatMessage({ id: `eveli.userProfile.tenantConfig.select.${value}`, defaultMessage: value })}
                onMouseDown={(e) => e.stopPropagation()}
                onDelete={(e) => handleDelete(e, value)}
              />))}
          </Box>
        )}>
          {tenantFeatureKeys.map((key) => (
            <MenuItem key={key} value={key}>
              {intl.formatMessage({ id: `eveli.userProfile.tenantConfig.select.${key}`, defaultMessage: key })}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
    </Box>
  );
}


