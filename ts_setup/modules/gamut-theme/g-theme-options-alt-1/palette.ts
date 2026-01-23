import { PaletteOptions } from "@mui/material";

export const palette: PaletteOptions = {
  primary: {
    main: '#1E3A5F',      // Deep executive navy
    light: '#4F6D8C',
    dark: '#14283F',
    contrastText: '#FFFFFF',
  },

  secondary: {
    main: '#7A8793',      // Cool steel gray-blue
    light: '#A7B2BC',
    dark: '#505A63',
    contrastText: '#FFFFFF',
  },

  background: {
    default: '#F3F5F7',   // Neutral, less blue-tinted
    paper: '#FFFFFF',
  },

  text: {
    primary: '#1A1F24',   // Near-black, strong readability
    secondary: '#5B6770',
  },

  divider: '#D6DCE1',

  success: {
    main: '#2E7D5B',      // Muted corporate green
  },
  warning: {
    main: '#C47A2C',      // Burnt amber (less playful than orange)
  },
  error: {
    main: '#B23A3A',      // Deep red, serious not bright
  },
  info: {
    main: '#2C5282',      // Strong blue, not teal
  },
}
