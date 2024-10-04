import React from 'react';
import { useTheme, useMediaQuery } from '@mui/material';
import { App } from '../AppAPI'

type BreakpointMode = "MOBILE" | "DESKTOP" | "TABLET";
interface BreakpointContextType {
  mode: BreakpointMode
}
const BreakpointContext = React.createContext<BreakpointContextType>({
  mode: "DESKTOP"
})


const BreakpointProvider: React.FC<{ app: App, children: React.ReactElement }> = (props) => {
  const theme = useTheme();
  const { config } = props.app;
  const { mobile, tablet } = config;

  const mode = React.useMemo(() => {
    const isMobile = mobile.breakpoint(theme, useMediaQuery);
    const isTablet = tablet.breakpoint(theme, useMediaQuery);
    //const desktop = config.desktop.breakpoint(theme, useMediaQuery);
    console.log(`portal: breakpoints, mobile: ${isMobile}, tablet: ${isTablet} `);
    let mode: BreakpointMode = "DESKTOP";
    if (isMobile) {
      mode = "MOBILE";
    } else if (isTablet) {
      mode = "TABLET";
    }
    return mode;
  }, [theme, mobile, tablet])

  return (<BreakpointContext.Provider value={{ mode }}>
    {props.children}
  </BreakpointContext.Provider>);
}

const useBreakpoint = () => {
  const result: BreakpointContextType = React.useContext(BreakpointContext);
  return result.mode;
}

export type { BreakpointContextType, BreakpointMode };

export { BreakpointProvider, BreakpointContext, useBreakpoint };