
import React from 'react';
import { GMarkdown } from '@dxs-ts/gamut-md';
import { GFormNoteValidationRoot } from './useUtilityClasses';
import { useThemeInfra } from './useUtilityClasses';

export interface GFormNoteValidationClasses {
  root: string;
}
export type GFormNoteValidationClassKey = keyof GFormNoteValidationClasses;

export interface GFormNoteValidationProps {
  id: string;
  label: string | undefined;
  style: 'error' | 'success' | 'warning' | 'info' | undefined;
  component?: React.ElementType<GFormNoteValidationProps>;
}


export const GFormNoteValidation: React.FC<GFormNoteValidationProps> = (initProps) => {
  const { ownerState, classes, props } = useThemeInfra(initProps);

  return (
    <GFormNoteValidationRoot ownerState={ownerState} as={ownerState.component} className={classes.root} severity={props.style}>
      <GMarkdown>{props.label}</GMarkdown>
    </GFormNoteValidationRoot>
  )
}