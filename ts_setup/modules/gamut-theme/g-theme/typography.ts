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

export const typography: TypographyOptions | ((palette: Palette) => TypographyOptions) = {

  fontFamily: '"Outfit", sans-serif',

  h1: {
    fontFamily: '"Outfit", sans-serif',
    fontSize: '2.5rem',
    fontWeight: 400,
    lineHeight: 1.375,
    [breakpoints.down('sm')]: {
      fontSize: '16pt',
      fontWeight: 400,
      lineHeight: 1.375,
      letterSpacing: 0.5,
      marginBottom: 2
    },
  },
  h2: {
    fontFamily: '"Outfit", sans-serif',
    fontSize: '1.9rem',
    fontWeight: 400,
    lineHeight: 1.375,
    [breakpoints.down('sm')]: {
      fontSize: '14pt',
      fontWeight: 400,
      lineHeight: 1.375,
      letterSpacing: 0.5,
      marginBottom: 2
    },
  },
  h3: {
    fontFamily: '"Outfit", sans-serif',
    fontSize: '1.5rem',
    fontWeight: 500,
    lineHeight: 1.375,
    [breakpoints.down('sm')]: {
      fontSize: '12pt',
      fontWeight: 400,
      lineHeight: 1.375,
      marginBottom: 2
    },
  },
  h4: {
    fontFamily: '"Outfit", sans-serif',
    fontSize: '1.125rem',
    fontWeight: 700,
    lineHeight: 1.375,
    [breakpoints.down('sm')]: {
      fontSize: '12pt',
      fontWeight: 700,
      lineHeight: 1.375,
    },
  },
  h5: {
    fontFamily: '"Outfit", sans-serif',
    fontSize: '12pt',
    fontWeight: 600,
    lineHeight: 1.375,
    [breakpoints.down('sm')]: {
      fontSize: '11pt',
      fontWeight: 600,
      lineHeight: 1.375,
    },
  },
  h6: {
    fontFamily: '"Outfit", sans-serif',
    fontWeight: 400,
    lineHeight: 1.375
  },
  body1: {
    fontFamily: '"Outfit", sans-serif',
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
    fontFamily: '"Outfit", sans-serif',
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
    fontFamily: '"Outfit", sans-serif',
    fontWeight: 500,
    lineHeight: 1.75,
    [breakpoints.down('sm')]: {
      fontSize: '10pt',
      fontWeight: 500,
      lineHeight: 1.375,
    },
  },
  subtitle2: {
    fontFamily: '"Outfit", sans-serif',
    fontWeight: 400,
    lineHeight: 1.57,
    color: '#65748B',
    [breakpoints.down('sm')]: {
      fontSize: '10pt',
      fontWeight: 400,
      lineHeight: 1.375,
    },
  },
  overline: {
    fontFamily: '"Outfit", sans-serif',
    fontWeight: 600,
    letterSpacing: '0.5px',
    lineHeight: 2.5,
    textTransform: 'uppercase'
  },
  caption: {
    fontFamily: '"Outfit", sans-serif',
    fontSize: '11pt',
    fontWeight: 500,
    lineHeight: 1.375,
    [breakpoints.down('sm')]: {
      fontSize: '10pt',
      fontWeight: 500,
      lineHeight: 1.375,
    },
  },
}
