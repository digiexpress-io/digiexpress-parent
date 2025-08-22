import React from 'react';
import { Box, Select, FormControl, SelectChangeEvent, MenuItem, InputLabel, Chip, Typography, useTheme } from '@mui/material';
import { useIntl } from 'react-intl';
import { UserProfileApi } from '@dxs-ts/user-profile'
import { tenant_features, TenantFeature, useTenantConfig } from '@dxs-ts/eveli-api';




export interface TenantConfigSelectProps {
  userProfile: UserProfileApi.UserProfile,
  onChange: (command: UserProfileApi.UserProfileUpdateCommand<any>) => void
}


export const TenantConfigSelect: React.FC<TenantConfigSelectProps> = ({ userProfile, onChange }) => {
  const intl = useIntl();
  const theme = useTheme();
  const { hardcodedFeatures } = useTenantConfig();

  const [selectedFeatures, setSelectedFeatures] = React.useState<string[]>(userProfile.tenantFeatures ?? []);
  const inputLabel = intl.formatMessage({ id: 'eveli.userProfile.tenantConfig.select', defaultMessage: 'Config option' });

  React.useEffect(() => {
    const userFeatures = userProfile.tenantFeatures ?? [];
    const intialSelected = [
      ...hardcodedFeatures,
      ...userFeatures.filter((feature => !hardcodedFeatures.includes(feature as TenantFeature))),
    ]
    setSelectedFeatures(intialSelected);
  }, []);

  function isHardcodedFeature(value: TenantFeature): boolean {
    return hardcodedFeatures.includes(value);
  }

  const sortedTenantFeatures = [...tenant_features].sort((a, b) => a.localeCompare(b));
  const sortedHardcodedFeatures = [...hardcodedFeatures].sort((a, b) => a.localeCompare(b));
  const filteredTenantFeatures = sortedTenantFeatures.filter(f => !hardcodedFeatures.includes(f));
  const combinedSortedFeatures = [...filteredTenantFeatures, ...sortedHardcodedFeatures];



  function handleChange(event: SelectChangeEvent<string[]>) {
    const value = event.target.value;
    const valueArray = typeof value === 'string' ? value.split(',') : value;

    const command: UserProfileApi.ChangeTenantFeatures = {
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
    if (hardcodedFeatures.includes(value as typeof hardcodedFeatures[number])) {
      return;
    }

    const updated = selectedFeatures.filter((item) => item !== value);
    setSelectedFeatures(updated);

    const command: UserProfileApi.ChangeTenantFeatures = {
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
            {(selectedFeatures).map((value: any) => {
              const isHardcoded = isHardcodedFeature(value);

              return (
                <Chip key={value} label={intl.formatMessage({ id: `eveli.userProfile.tenantConfig.select.${value}`, defaultMessage: value })}
                  onMouseDown={(e: React.MouseEvent) => e.stopPropagation()}
                  onDelete={!isHardcoded ? (e) => handleDelete(e, value) : undefined}
                  color={!isHardcoded ? 'info' : 'warning'}
                />
              )
            })}
          </Box>
          )}>
          {combinedSortedFeatures.map((key) => {
            const isHardcoded = isHardcodedFeature(key);
            return (
              <MenuItem key={key} value={key} disabled={isHardcoded}>
                <Box display='flex' flexDirection='column' width='100%'>
                  <Typography fontWeight='bold'>{intl.formatMessage({ id: `eveli.userProfile.tenantConfig.select.${key}`, defaultMessage: key })}</Typography>
                  <Typography variant='subtitle2' sx={{ color: theme.palette.primary.main }}>{intl.formatMessage({ id: `eveli.userProfile.tenantConfig.select.${key}.desc`, defaultMessage: key })}</Typography>
                </Box>
              </MenuItem>
            )
          })}
        </Select>
      </FormControl>
    </Box>
  );
}


