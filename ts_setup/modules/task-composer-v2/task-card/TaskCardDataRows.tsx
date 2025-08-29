import React from 'react';
import { useTheme, Divider, Grid2, Typography } from "@mui/material";
import { TaskCardStyleDefinition } from "./cardThemeConfig";


interface TaskCardDataRowTextProps {
  label: string;
  value: string | string[] | undefined;
  style: TaskCardStyleDefinition;
}

export const TaskCardDataRowText: React.FC<TaskCardDataRowTextProps> = ({ label, value, style }) => {
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

export const TaskCardDataRowElement: React.FC<{ label: string, value: React.ReactNode, style: TaskCardStyleDefinition }> = ({ label, value, style }) => {
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

