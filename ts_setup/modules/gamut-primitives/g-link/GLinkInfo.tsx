import React from 'react';
import { Typography, styled, useThemeProps, generateUtilityClass } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { GMarkdown } from '@dxs-ts/gamut-md';
import { GOverridableComponent } from '@dxs-ts/gamut-api';



export interface GLinkInfoClasses {
  root: string;
}
export type GLinkInfoClassKey = keyof GLinkInfoClasses;

export interface GLinkInfoProps {
  label?: string | undefined;
  value: string;
  component?: GOverridableComponent<GLinkInfoProps>;
}

const useUtilityClasses = (ownerState: GLinkInfoProps) => {
  const slots = { root: ['root'] };
  const getUtilityClass = (slot: string) => generateUtilityClass('GLinkInfo', slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GLinkInfo: React.FC<GLinkInfoProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: 'GLinkInfo',
  });
  const classes = useUtilityClasses(props);
  const ownerState = {
    ...props
  }
  const Root = props.component ?? GLinkInfoRoot

  // Split content at markdown-style line breaks (double space)
  const lines = props.value.split("  ");

  return (
    <Root className={classes.root} ownerState={ownerState}>
      <>
        <Typography fontWeight='bold' mb={1}>{props.label}</Typography>
        {lines.map((line, index) => (<GMarkdown key={index}>{line}</GMarkdown>))}
      </>
    </Root>)
}

const GLinkInfoRoot = styled("div", {
  name: 'GLinkInfo',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
    ];
  },
})<{ ownerState: GLinkInfoProps }>(({ theme }) => {
  return {
  };
});