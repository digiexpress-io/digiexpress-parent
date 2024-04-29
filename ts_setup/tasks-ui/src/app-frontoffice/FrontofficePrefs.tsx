import React from 'react';

import { PreferenceInit, PreferenceProvider, usePreference } from 'descriptor-prefs';
import Burger from 'components-burger';
import { UserProfileAndOrg } from 'descriptor-access-mgmt';
import { parsePreference } from 'descriptor-prefs';
import { useSecondary } from './Views/Secondary';


const CONFIG_ID = 'app-frontoffice'; //settings id
const SECONDARY_MENU_ITEM = CONFIG_ID + ".secondary.menu-item";
const SECONDARY_DRAWER = CONFIG_ID + ".secondary.drawer";

function initPrefs(): PreferenceInit {
  return {
    id: CONFIG_ID,
    fields: [], // no sorting or visibility
    config: {
      dataId: SECONDARY_MENU_ITEM,  
      value: "myProfile" // default to my profile
    }
  }
}

export function getDrawerOpen( profile: UserProfileAndOrg) {
  const prefs = parsePreference(CONFIG_ID, profile.user);
  const config = prefs.getConfig(SECONDARY_DRAWER);

  if(config) {
    return config.value === 'true';
  }
  return true;
}

export function useSecondaryMenuItem() {
  const { pref, withConfig } = usePreference();
  const currentValue = pref.getConfig(SECONDARY_MENU_ITEM)?.value ?? 'mytasks';

  function setNextValue(nextMenuSelected: string) {
    withConfig({ 
      dataId: SECONDARY_MENU_ITEM,
      value: nextMenuSelected
    });
  }

  return { currentValue, setNextValue };
}

export const InitNav: React.FC<{ }> = ({}) => {
  const { pref } = usePreference();
  const { callbacks } = useSecondary();
  
  React.useEffect(() => {
    const config = pref.getConfig(SECONDARY_MENU_ITEM);
    if(!config) {
      return;
    }

    const key = config.value;
    const init = Object.entries(callbacks)
      .filter(([name]) => name === key)
      .map(([_name, handler]) => handler);
    if(init.length) {
      init[0]();
    }
  }, []);
  
  return (<></>);
}

export const SyncDrawer: React.FC<{ }> = ({}) => {
  const { withConfig } = usePreference();
  const { session } = Burger.useDrawer();
  const { drawer } = session;

  React.useEffect(() => {
    withConfig({ 
      dataId: SECONDARY_DRAWER,
      value: drawer + ""
    });
  }, [drawer]);
  
  return (<></>);
}
export const FrontofficePrefs: React.FC<{ children: React.ReactNode }> = ({children}) => {
  return (<PreferenceProvider init={initPrefs()}><>{children}</></PreferenceProvider>);
}

