import { ThemeOptions } from '@mui/material';

import { components_g } from './components-g';
import { components_mui } from './components-mui';
import { palette } from './palette';
import { typography } from './typography';

export const GThemeOptionsAlt1: ThemeOptions = { palette, typography, components: { ...components_g, ...components_mui } };