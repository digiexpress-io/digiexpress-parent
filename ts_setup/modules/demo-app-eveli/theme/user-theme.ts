
import { createTheme } from '@mui/material';
import { eveliTheme } from '@dxs-ts/eveli-ide';
import { components_eveli } from './components-eveli';



export const userTheme = createTheme({
  palette: eveliTheme.palette,
  typography: eveliTheme.typography,
  components: { ...eveliTheme.components, ...components_eveli }
});