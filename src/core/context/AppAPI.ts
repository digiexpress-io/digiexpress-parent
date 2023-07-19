import { Theme, Options } from '@mui/material';
import { SxProps } from '@mui/system';
import { BreakpointMode } from './breakpoint/BreakpointContext';


type AppId = string;
type MediaQuery = (queryInput: string | ((theme: Theme) => string), options?: Options) => boolean;
type StyleQuery = (props: { drawerOpen: boolean }) => SxProps;

interface App {
  id: AppId;
  components:(mode: BreakpointMode) => ({
    toolbar: React.ElementType<ToolbarProps>;
    primary: React.ElementType<PrimaryProps>;
    secondary: React.ElementType<SecondaryProps>;
  }),
  config: {
    mobile: BreakpointConfig,
    tablet: BreakpointConfig,
    desktop: BreakpointConfig
  }
}

interface BreakpointConfig {
  breakpoint: (theme: Theme, mediaQuery: MediaQuery) => boolean;
  drawerWidth: (theme: Theme, mediaQuery: MediaQuery) => string | number;
  toolbarHeight: number;
  main: StyleQuery;
  secondary: StyleQuery;
  drawerOpen: boolean;  
}

interface ToolbarProps {
  //sx: SxProps
}

interface PrimaryProps {
  sx: SxProps

}
interface SecondaryProps {
  //sx: SxProps
}

export type { App, AppId, MediaQuery, StyleQuery, BreakpointConfig, ToolbarProps, PrimaryProps, SecondaryProps };
