import React from 'react';

import { PreferenceInit, createPrefContext } from 'descriptor-prefs';

const XPrefContext = createPrefContext();

export const XPrefProvider: React.FC<{ 
  children: React.ReactElement;
  init: PreferenceInit;
}> = ({ children, init }) => {
  return (<XPrefContext.Provider init={init}>{children}</XPrefContext.Provider>);
}

export const useXPref = XPrefContext.usePreference;