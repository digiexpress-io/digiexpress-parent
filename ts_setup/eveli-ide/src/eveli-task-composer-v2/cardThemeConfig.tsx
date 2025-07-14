import { CSSProperties} from 'react';
import { useTheme } from '@mui/material';

export type TaskCardStyleKey = 'compact' | 'default' | 'comfortable';

const singleCol = { xs: 12, sm: 12, md: 12, lg: 12, xl: 12 };

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
  comfortable: { xs: 12, sm: 12, md: 12, lg: 12, xl: 12 },
};

export const TASK_CARD_STYLE_LABELS: Record<TaskCardStyleKey, string> = {
  compact: 'Compact',
  default: 'Default',
  comfortable: 'Comfortable',
};


export const useTaskCardThemeConfig = (reviewOpen?: boolean): Record<TaskCardStyleKey, TaskCardStyleDefinition> => {
  const theme = useTheme();

  return {
    compact: {
      titleTypography: theme.typography.body2,
      bodyTypography: theme.typography.subtitle2,
      bodyTypographySmall: theme.typography.caption,
      cardSpacing: theme.spacing(2),
      dataRowGridSizes: {
        label: { xs: 4, sm: 4, md: 4, lg: 4, xl: 4 },
        value: { xs: 8, sm: 8, md: 8, lg: 8, xl: 8 }
      }
    },
    default: {
      titleTypography: theme.typography.h4,
      bodyTypography: theme.typography.body2,
      bodyTypographySmall: theme.typography.subtitle2,
      cardSpacing: theme.spacing(3),
      dataRowGridSizes: {
        label: { xs: 4, sm: 4, md: 4, lg: 4, xl: 4 },
        value: { xs: 8, sm: 8, md: 8, lg: 8, xl: 8 }
      }
    },
    comfortable: {
      titleTypography: {
        ...theme.typography.h3,
        paddingTop: theme.spacing(1),
        paddingBottom: theme.spacing(1)
      },
      bodyTypography: {
        ...theme.typography.h4,
        paddingTop: theme.spacing(0.5),
        paddingBottom: theme.spacing(0.5)
      },
      bodyTypographySmall: theme.typography.body2,
      cardSpacing: theme.spacing(3),

      dataRowGridSizes: {
        label: reviewOpen ? singleCol : { xs: 3, sm: 3, md: 3, lg: 3, xl: 3 },
        value: reviewOpen ? singleCol : { xs: 9, sm: 9, md: 9, lg: 9, xl: 9 },
      },
    },
  };
};

export const flashyCardColorsById: Record<string, { flashyBackground: string; flashyBorder: string }> = {
  'task-main': {
    flashyBackground: '#1565c0',     // Dark Blue
    flashyBorder: '#0d47a1',
  },
  'status-priority': {
    flashyBackground: '#b71c1c',     // Dark Red
    flashyBorder: '#7f0000',
  },
  'task-form-summary': {
    flashyBackground: '#303f9f',     // Dark Indigo
    flashyBorder: '#1a237e',
  },
  'assignees-roles': {
    flashyBackground: '#ef6c00',     // Darker Orange (↑ contrast)
    flashyBorder: '#e65100',
  },
  'customer-messages': {
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
  'task-meta': {
    flashyBackground: '#4e342e',     // Deep Brown
    flashyBorder: '#3e2723',
  },
  default: {
    flashyBackground: '#1565c0',
    flashyBorder: '#0d47a1',
  },
};
