import React from 'react';
import { Box, Select, FormControl, SelectChangeEvent, MenuItem, InputLabel, Chip, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { PrefsApi } from '@/api-prefs';
import { EveliFeatureMapping } from '@/api-tenant-config';




export interface TenantConfigSelectProps {
  userProfile: PrefsApi.UserProfile,
  onChange: (command: PrefsApi.UserProfileUpdateCommand<any>) => void
}


export const TenantConfigSelect: React.FC<TenantConfigSelectProps> = ({ userProfile, onChange }) => {
  const intl = useIntl();

  const [selectedFeatures, setSelectedFeatures] = React.useState<string[]>(userProfile.tenantFeatures ?? []);
  const tenantFeatureKeys = Object.keys(EveliFeatureMapping);
  const inputLabel = intl.formatMessage({ id: 'eveli.userProfile.tenantConfig.select', defaultMessage: 'Config option' });


  function handleChange(event: SelectChangeEvent<string[]>) {
    const value = event.target.value;
    const valueArray = typeof value === 'string' ? value.split(',') : value;

    const command: PrefsApi.ChangeTenantFeatures = {
      commandType: 'ChangeTenantFeatures',
      id: userProfile.id,
      tenantFeatures: valueArray
    };

    onChange(command);
    setSelectedFeatures(valueArray);
    console.log("Selected", valueArray)
  }


  function handleDelete(e: React.MouseEvent, value: string) {
    e.stopPropagation();
    const updated = selectedFeatures.filter((item) => item !== value);
    setSelectedFeatures(updated);

    const command: PrefsApi.ChangeTenantFeatures = {
      commandType: 'ChangeTenantFeatures',
      id: userProfile.id,
      tenantFeatures: updated
    };

    onChange(command);
  }

  return (
    <Box sx={{ minWidth: 120 }}>
      <Typography variant='subtitle2' mb={3}>
        {intl.formatMessage({
          id: `eveli.userProfile.tenantConfig.select.title`,
          defaultMessage: 'Personalise your user experience by adding and removing features'
        })}
      </Typography>
      <FormControl fullWidth>
        <InputLabel>{inputLabel}</InputLabel>
        <Select multiple value={selectedFeatures} label={inputLabel} onChange={handleChange}
          renderValue={() => (<Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
            {(selectedFeatures).map((value) => (
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


