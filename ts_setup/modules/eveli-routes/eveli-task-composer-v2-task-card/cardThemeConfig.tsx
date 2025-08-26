import { CSSProperties } from 'react';
import { useTheme } from '@mui/material';
import { TaskCardId, TaskCardStyleKey, useCardConfig } from './CardConfigContext';



const singleCol = { xs: 12, sm: 12, md: 12, lg: 12, xl: 12 };
const doubleCol = { xs: 6, sm: 6, md: 6, lg: 6, xl: 6 };

export interface TaskCardStyleDefinition {
  titleTypography: CSSProperties;
  bodyTypography: CSSProperties;
  bodyTypographySmall: CSSProperties;
  dataRowGridSizes: {
    label: { xs: number; sm: number; md: number; lg: number; xl: number };
    value: { xs: number; sm: number; md: number; lg: number; xl: number };
  };
  cardSpacing?: string;
}
export const taskCardGridSize: Record<TaskCardStyleKey | 'singleCol', { xs: number; sm: number; md: number; lg: number; xl: number }> = {
  singleCol,
  compact: { xs: 12, sm: 6, md: 4, lg: 3, xl: 4 },
  default: { xs: 12, sm: 8, md: 6, lg: 6, xl: 6 },
  large: singleCol,
};

export const useTaskCardThemeConfig = (): Record<TaskCardStyleKey, TaskCardStyleDefinition> => {
  const theme = useTheme();
  const { isReviewOpen } = useCardConfig();

  return {
    compact: {
      titleTypography: theme.typography.h5,
      bodyTypography: theme.typography.subtitle2,
      bodyTypographySmall: theme.typography.caption,
      cardSpacing: theme.spacing(1),
      dataRowGridSizes: {
        label: { xs: 4, sm: 4, md: 4, lg: 4, xl: 4 },
        value: { xs: 8, sm: 8, md: 8, lg: 8, xl: 8 }
      }
    },
    default: {
      titleTypography: theme.typography.h4,
      bodyTypography: theme.typography.body2,
      bodyTypographySmall: theme.typography.subtitle2,
      cardSpacing: theme.spacing(1),
      dataRowGridSizes: {
        label: { xs: 4, sm: 4, md: 4, lg: 4, xl: 4 },
        value: { xs: 8, sm: 8, md: 8, lg: 8, xl: 8 }
      }
    },
    large: {
      titleTypography: {
        ...theme.typography.h5,
        paddingTop: theme.spacing(1),
        paddingBottom: theme.spacing(1)
      },
      bodyTypography: {
        ...theme.typography.h5,
        paddingTop: theme.spacing(0.5),
        paddingBottom: theme.spacing(0.5),
        fontWeight: 400
      },
      bodyTypographySmall: theme.typography.body2,
      cardSpacing: theme.spacing(2),

      dataRowGridSizes: {
        label: isReviewOpen ? doubleCol : { xs: 3, sm: 3, md: 3, lg: 3, xl: 3 },
        value: isReviewOpen ? doubleCol : { xs: 9, sm: 9, md: 9, lg: 9, xl: 9 },
      },
    },
  };
};

export const flashyCardColorsById: Record<TaskCardId, { flashyBackground: string; flashyBorder: string }> = {
  'task_main': {
    flashyBackground: '#1565c0',     // Dark Blue
    flashyBorder: '#0d47a1',
  },
  'task_main_alt': {
    flashyBackground: '#1565c0',     // Dark Blue
    flashyBorder: '#0d47a1',
  },
  'status_priority': {
    flashyBackground: '#b71c1c',     // Dark Red
    flashyBorder: '#7f0000',
  },
  'task_form_summary': {
    flashyBackground: '#232c70',     // Dark Indigo
    flashyBorder: '#181e50',
  },
  'assignees_roles': {
    flashyBackground: '#ef6c00',     // Dark Orange 
    flashyBorder: '#e65100',
  },
  'customer_messages': {
    flashyBackground: '#6a1b9a',     // Deep Purple
    flashyBorder: '#4a0072',
  },
  'files': {
    flashyBackground: '#2e7d32',     // Dark Green
    flashyBorder: '#1b5e20',
  },
  'feedback': {
    flashyBackground: '#ad1457',     // Deep Pink
    flashyBorder: '#880e4f',
  },
  'notes': {
    flashyBackground: '#37474f',     // Dark Slate Gray
    flashyBorder: '#263238',
  },
  'task_meta': {
    flashyBackground: '#4e342e',     // Deep Brown
    flashyBorder: '#3e2723',
  }
};
