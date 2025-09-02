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

export const flashyCardColorsById: Record<TaskCardId, { flashyBackground: string; flashyBorder: string; contrastText: string }> = {
  'task_main': {
    flashyBackground: '#E4F0FB', // brighter slate blue
    flashyBorder: '#1F4E79',     // deep corporate blue
    contrastText: '#1C1C1C',
  },
  'task_main_alt': {
    flashyBackground: '#D6EBF8', // bright sky blue
    flashyBorder: '#2E70B3',     // medium professional blue
    contrastText: '#1C1C1C',
  },
  'status_priority': {
    flashyBackground: '#F8D6D6', // brighter muted red
    flashyBorder: '#B73737',     // professional deep red
    contrastText: '#1C1C1C',
  },
  'task_form_summary': {
    flashyBackground: '#E3D8F5', // brighter corporate lilac
    flashyBorder: '#4B4F7C',     // muted violet accent
    contrastText: '#1C1C1C',
  },
  'assignees_roles': {
    flashyBackground: '#D2E4F7', // brighter slate blue
    flashyBorder: '#1E5FA8',     // strong corporate blue
    contrastText: '#1C1C1C',
  },
  'customer_messages': {
    flashyBackground: '#E1E8F5', // brighter light blue-gray
    flashyBorder: '#383F56',     // dark muted blue
    contrastText: '#1C1C1C',
  },
  'files': {
    flashyBackground: '#EAD8F5', // light vibrant purple
    flashyBorder: '#5A4B6A',     // muted purple accent
    contrastText: '#1C1C1C',
  },
  'feedback': {
    flashyBackground: '#FFE6D6', // brighter muted coral
    flashyBorder: '#E5946B',     // muted coral accent
    contrastText: '#1C1C1C',
  },
  'notes': {
    flashyBackground: '#E1E8F5', // brighter gray-blue
    flashyBorder: '#2E5C8A',     // deep corporate blue
    contrastText: '#1C1C1C',
  },
  'task_meta': {
    flashyBackground: '#D8E2F0', // slightly brighter gray-blue
    flashyBorder: '#6B4F7C',     // muted purple accent
    contrastText: '#1C1C1C',
  },
  'transfer': {
    flashyBackground: '#D6F2EB', // soft mint-teal background
    flashyBorder: '#3A9D8F',     // deeper teal accent
    contrastText: '#1C1C1C',
}
};








