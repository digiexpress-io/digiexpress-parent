import { Palette } from "@mui/material/styles";
import { TypographyOptions } from '@mui/material/styles/createTypography';
import { createTheme } from "@mui/system";

const breakpointDefs = {
  values: {
    xs: 0,
    sm: 480,
    md: 768,
    lg: 1024,
    xl: 1200
  },
};
export const breakpoints = createTheme({ breakpoints: breakpointDefs }).breakpoints;

export const typography: TypographyOptions | ((palette: Palette) => TypographyOptions) = (palette: Palette) => ({

  fontFamily: '"DM Serif Text", serif',

  h1: {
    fontFamily: '"DM Serif Text", serif',
    fontSize: '2.5rem',
    fontWeight: 900,
    lineHeight: 1.375,
    color: palette.primary.dark,
    [breakpoints.down('sm')]: {
      fontSize: '16pt',
      fontWeight: 900,
      lineHeight: 1.375,
      letterSpacing: 0.5,
      marginBottom: 2,
      color: palette.primary.dark,
    },
  },
  h2: {
    fontFamily: '"DM Serif Text", serif',
    fontSize: '1.9rem',
    fontWeight: 700,
    lineHeight: 1.375,
    color: palette.primary.dark,
    marginTop: 10,
    [breakpoints.down('sm')]: {
      fontSize: '14pt',
      fontWeight: 700,
      lineHeight: 1.375,
      letterSpacing: 0.5,
      marginBottom: 2,
      color: palette.primary.dark,
    },
  },
  h3: {
    fontFamily: '"DM Serif Text", serif',
    fontSize: '1.5rem',
    fontWeight: 500,
    lineHeight: 1.375,
    marginTop: 10,
    color: palette.primary.dark,
    [breakpoints.down('sm')]: {
      fontSize: '12pt',
      fontWeight: 500,
      lineHeight: 1.375,
      marginBottom: 2,
      marginTop: 10,
      color: palette.primary.dark,
    },
  },
  h4: {
    fontFamily: '"DM Serif Text", serif',
    fontSize: '1.125rem',
    fontWeight: 400,
    lineHeight: 1.375,
    [breakpoints.down('sm')]: {
      fontSize: '12pt',
      fontWeight: 700,
      lineHeight: 1.375,
    },
  },
  h5: {
    fontFamily: '"DM Serif Text", serif',
    fontSize: '12pt',
    fontWeight: 400,
    lineHeight: 1.375,
    [breakpoints.down('sm')]: {
      fontSize: '12pt',
      fontWeight: 600,
      lineHeight: 1.375,
    },
  },
  h6: {
    fontFamily: '"DM Serif Text", serif',
    fontWeight: 400,
    lineHeight: 1.375
  },
  body1: {
    fontFamily: '"DM Serif Text", serif',
    fontWeight: 500,
    fontSize: '1.125rem',
    lineHeight: 1.4,
    [breakpoints.down('sm')]: {
      fontSize: '12pt',
      fontWeight: 500,
      lineHeight: 1.375,
    },
  },
  body2: {
    fontFamily: '"DM Serif Text", serif',
    fontWeight: 500,
    fontSize: '1rem',
    lineHeight: 1.4,
    [breakpoints.down('sm')]: {
      fontSize: '11pt',
      fontWeight: 500,
      lineHeight: 1.375,
    },
  },
  subtitle1: {
    fontFamily: '"DM Serif Text", serif',
    fontWeight: 500,
    lineHeight: 1.75,
    [breakpoints.down('sm')]: {
      fontSize: '10pt',
      fontWeight: 500,
      lineHeight: 1.375,
    },
  },
  subtitle2: {
    fontFamily: '"DM Serif Text", serif',
    fontWeight: 400,
    lineHeight: 1.57,
    color: palette.text.secondary,
    [breakpoints.down('sm')]: {
      fontSize: '10pt',
      fontWeight: 400,
      lineHeight: 1.375,
    },
  },
  overline: {
    fontFamily: '"DM Serif Text", serif',
    fontWeight: 600,
    letterSpacing: '0.5px',
    lineHeight: 2.5,
    textTransform: 'uppercase'
  },
  caption: {
    fontFamily: '"DM Serif Text", serif',
    fontSize: '11pt',
    fontWeight: 500,
    lineHeight: 1.375,
    [breakpoints.down('sm')]: {
      fontSize: '10pt',
      fontWeight: 500,
      lineHeight: 1.375,
    },
  },
})
