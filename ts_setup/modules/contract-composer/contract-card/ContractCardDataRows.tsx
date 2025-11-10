import React from 'react';
import { useTheme, Divider, Grid2, Typography, Box, alpha } from "@mui/material";
import { ContractCardStyleDefinition } from "./cardThemeConfig";
import { useIntl } from 'react-intl';


interface ContractCardDataRowTextProps {
  label: string;
  value: string | string[] | undefined;
  style: ContractCardStyleDefinition;
}

export const ContractCardDataRowText: React.FC<ContractCardDataRowTextProps> = ({ label, value, style }) => {
  const theme = useTheme();

  return (<>
    <Grid2 container margin={theme.spacing(0.5)}>
      <Grid2 size={style.dataRowGridSizes.label}>
        <Typography sx={{ ...style.bodyTypography, fontWeight: 500, whiteSpace: 'normal', wordWrap: 'break-word' }}>
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

export const ContractCardDataRowElement: React.FC<{ label: string, value: React.ReactNode, style: ContractCardStyleDefinition }> = ({ label, value, style }) => {
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
            marginRight: 1
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


export const ContractCardDataRowParty: React.FC<{ titleLabel: string, valueLabel: string, children: React.ReactNode, style: ContractCardStyleDefinition }> = ({ titleLabel, valueLabel, children, style }) => {
  const theme = useTheme();

  return (

    <Box sx={{ marginBottom: theme.spacing(2) }}>
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

export const ContractCardTransitivesRow: React.FC<{ createdAt: string, updatedAt: string }> = ({ createdAt, updatedAt }) => {
  const intl = useIntl();
  const theme = useTheme();

  return (
    <Box display='flex' gap={theme.spacing(1)} marginTop={theme.spacing(1)} justifySelf='flex-end'>
      <Typography variant='caption'>{intl.formatMessage({ id: 'contractcard.transitives.createdAt' })}{": "}{createdAt}</Typography>
      <Typography variant='caption'>{intl.formatMessage({ id: 'contractcard.transitives.updatedAt' })}{": "}{updatedAt}</Typography>
    </Box>
  )
}


