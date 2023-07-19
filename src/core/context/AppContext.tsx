import React from 'react';

import * as API from './AppAPI';

import { BreakpointProvider, useBreakpoint } from './breakpoint/BreakpointContext';
import { DrawerProvider } from './drawer/DrawerContext';
import { TabsProvider } from './tabs/TabsContext';
import { SecondaryProvider } from './secondary/SecondaryContext';
import { Container } from '../layout-calc/Layout';


interface AppProviderProps {
  secondaryOpen?: string;
  children: API.App;
}


const AppInit: React.FC<{ children: API.App }> = ({ children }) => {
  const mode = useBreakpoint();
  return React.useMemo(() => {
    console.log(`portal: app container/layout Init: '${children.id}', mode: ${mode}`);

    const { config, components } = children;
    const { primary, secondary, toolbar } = components(mode);

    if (mode === 'MOBILE') {
      return (<Container primary={primary} secondary={secondary} toolbar={toolbar} config={config.mobile} />);
    } else if (mode === "TABLET") {
      return (<Container primary={primary} secondary={secondary} toolbar={toolbar} config={config.tablet} />)
    }

    return (<Container primary={primary} secondary={secondary} toolbar={toolbar} config={config.desktop} />);
  }, [children, mode]);
}


const AppLayout: React.FC<AppProviderProps> = (props) => {
  const { secondaryOpen, children } = props;
  const { id } = children;
  const mode = useBreakpoint();
  let drawerOpen = true;
  if(mode === "MOBILE") {
    drawerOpen = children.config.mobile.drawerOpen;
  } else if(mode === "TABLET") {
    drawerOpen = children.config.tablet.drawerOpen;
  } else {
    drawerOpen = children.config.desktop.drawerOpen;
  }
  
  return React.useMemo(() => {
    return (<DrawerProvider drawerOpen={drawerOpen}>
      <TabsProvider appId={id}>
        <SecondaryProvider appId={id} secondary={secondaryOpen}>
          <AppInit children={children} />
        </SecondaryProvider>
      </TabsProvider>
    </DrawerProvider>)
  }, [id, secondaryOpen, children, drawerOpen]);
}

const AppProvider: React.FC<AppProviderProps> = (props: AppProviderProps) => {

  const { secondaryOpen, children } = props;

  return React.useMemo(() => {
    console.log("portal: App Provider Init");
    return (
      <BreakpointProvider app={children}>
        <AppLayout children={children} secondaryOpen={secondaryOpen}/>
      </BreakpointProvider>
    )
  }, [secondaryOpen, children]);
};

export type { AppProviderProps };
export { AppProvider };

