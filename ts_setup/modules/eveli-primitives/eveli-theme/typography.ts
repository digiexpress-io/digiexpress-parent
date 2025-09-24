import { Palette } from '@mui/material/styles';
import { TypographyOptions } from '@mui/material/styles/createTypography';


export const typography: TypographyOptions | ((palette: Palette) => TypographyOptions) = {
  fontFamily: '"Ubuntu", sans-serif',
  h1: {
    fontSize: "2rem",
    lineHeight: 1.5,
    fontWeight: 600,
  },
  h2: {
    fontSize: "1.9rem",
    lineHeight: 1,
    fontWeight: 500,
    paddingTop: 15,
    paddingBottom: 15,
  },
  h3: {
    fontSize: "1.6rem",
    fontWeight: 300,
    lineHeight: 1,
    paddingTop: 15,
    paddingBottom: 15,
  },
  h4: {
    fontSize: "1.3rem",
    lineHeight: 1,
    fontWeight: 300
  },
  h5: {
    fontSize: "1.1rem",
    fontWeight: 300
  },
  h6: {
    fontWeight: 300
  },
  body1: {
    fontSize: "1rem",
    fontWeight: 400,
  },
  body2: {
    fontSize: "1rem",
    fontWeight: 400,
  },
  caption: {
    fontSize: "0.7rem",
    fontWeight: 500,
  },
  subtitle2: {
    fontSize: '10pt',
    fontWeight: 400,
  }
}
