import React from 'react';
import { useThemeInfra, GFormNoteRoot } from './useThemeInfra';
import { GMarkdown } from '../g-md'

export interface GFormNoteClasses {
  root: string;
}
export type GFormNoteClassKey = keyof GFormNoteClasses;

export interface GFormNoteProps {
  id: string;
  label: string | undefined;
  component?: React.ElementType<GFormNoteProps>;
}

export const GFormNote: React.FC<GFormNoteProps> = (initProps) => {
  const { ownerState, classes, props } = useThemeInfra(initProps);

  return (
    <GFormNoteRoot ownerState={ownerState} as={ownerState.component} className={classes.root} severity='info'>
      <GMarkdown>{props.label}</GMarkdown>
    </GFormNoteRoot>)
}
