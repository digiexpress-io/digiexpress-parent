import React from 'react';
import { useTheme, Divider, Grid2, Typography, Box, alpha, List, ListItem, ListItemText, ListSubheader } from "@mui/material";
import { CockpitCardStyleDefinition } from "./cockpitCardThemeConfig";
import { useIntl } from 'react-intl';


interface CockpitCardDataRowTextProps {
  label: string;
  value: string | string[] | undefined;
  style: CockpitCardStyleDefinition;
}

export const CockpitCardDataRowText: React.FC<CockpitCardDataRowTextProps> = ({ label, value, style }) => {
  const theme = useTheme();

  return (<>
    <Grid2 container margin={theme.spacing(0.5)}>
      <Grid2 size={style.dataRowGridSizes.label}>
        <Typography sx={{
          ...style.bodyTypography,
          fontWeight: 500,
          marginRight: theme.spacing(1),
          whiteSpace: 'normal',
          wordWrap: 'break-word'
        }}>
          {label}
        </Typography>
      </Grid2>

      <Grid2 size={style.dataRowGridSizes.value}>
        <Typography sx={{ ...style.bodyTypography, whiteSpace: 'normal', wordWrap: 'break-word' }}>
          {value}
        </Typography>
      </Grid2>
    </Grid2>
    <Divider />
  </>
  )
}

export const CockpitCardDataRowElement: React.FC<{ label: string, value: React.ReactNode, style: CockpitCardStyleDefinition }> = ({ label, value, style }) => {
  const theme = useTheme();

  return (<>
    <Grid2 container margin={theme.spacing(0.5)}>
      <Grid2 size={style.dataRowGridSizes.label}>
        <Typography
          sx={{
            ...style.bodyTypography,
            fontWeight: 500,
            whiteSpace: 'normal',
            wordWrap: 'break-word',
            marginRight: theme.spacing(1)
          }}>
          {label}
        </Typography>
      </Grid2>

      <Grid2 size={style.dataRowGridSizes.value}>
        {value}
      </Grid2>
    </Grid2>
  </>
  )
}


export const CockpitCardDataRowGrouped: React.FC<{ titleLabel: string, valueLabel: string, children: React.ReactNode, style: CockpitCardStyleDefinition }> = ({ titleLabel, valueLabel, children, style }) => {
  const theme = useTheme();

  return (

    <Box margin={theme.spacing(0.5)}>
      <Box display='flex' alignItems='baseline'>
        <Typography sx={{
          ...style.bodyTypography,
          fontWeight: 500,
          whiteSpace: 'normal',
          wordWrap: 'break-word',
          marginRight: theme.spacing(1)
        }}>
          {titleLabel}
        </Typography>
        <Typography sx={{ ...style.bodyTypography }}>{valueLabel}</Typography>
      </Box>

      <Box marginLeft={theme.spacing(3)} paddingLeft={theme.spacing(1)} borderLeft={`2px solid ${alpha(theme.palette.primary.main, 0.5)}`}>
        {children}
      </Box>
    </Box>
  )
}

export const CockpitCardTransitivesRow: React.FC<{ createdAt: string, updatedAt: string }> = ({ createdAt, updatedAt }) => {
  const intl = useIntl();
  const theme = useTheme();

  return (
    <Box display='flex' gap={theme.spacing(1)} marginTop={theme.spacing(1)} justifyContent='end'>
      <Typography variant='caption'>{intl.formatMessage({ id: 'cockpitcard.transitives.createdAt' })}{": "}{createdAt}</Typography>
      <Typography variant='caption'>{intl.formatMessage({ id: 'cockpitcard.transitives.updatedAt' })}{": "}{updatedAt}</Typography>
    </Box>
  )
}

export const CockpitCardDataList: React.FC<{
  label: string,
  value: string,
  index: number,
  labelColHeader?: string | undefined,
  valueColheader?: string | undefined
}> = ({ label, value, index, labelColHeader, valueColheader }) => {
  const theme = useTheme();

  return (<>
    {(index === 0 && labelColHeader && valueColheader) && (
      <List dense sx={{ padding: theme.spacing(0.5) }}>
        <ListItem>
          <Typography sx={{ width: '70%', fontWeight: 500 }}>{labelColHeader}</Typography>
          <Typography sx={{ width: '30%', fontWeight: 500 }}>{valueColheader}</Typography>
        </ListItem>
      </List>
    )}

    <List dense sx={{ padding: theme.spacing(0.5) }}>
      <ListItem dense sx={{ backgroundColor: index % 2 === 0 ? theme.palette.background.paper : theme.palette.action.hover }}>
        <ListItemText sx={{ width: '70%' }}>{label}</ListItemText>
        <ListItemText sx={{ width: '30%' }}>{value}</ListItemText>
      </ListItem>
    </List>
  </>
  )
}