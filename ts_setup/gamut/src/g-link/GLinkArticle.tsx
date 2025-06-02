import React from 'react';
import { Link, styled, useThemeProps } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import generateUtilityClass from '@mui/utils/generateUtilityClass';

import { GOverridableComponent } from '../g-override';

export interface GLinkArticleClasses {
  root: string;
}
export type GLinkArticleClassKey = keyof GLinkArticleClasses;

export interface GLinkArticleProps {
  label: string;
  value: string;
  onClick: () => void;
  component?: GOverridableComponent<GLinkArticleProps>;
}

const useUtilityClasses = (ownerState: GLinkArticleProps) => {
  const slots = { root: ['root'] };
  const getUtilityClass = (slot: string) => generateUtilityClass('GLinkArticle', slot);
  return composeClasses(slots, getUtilityClass, {});
}

export const GLinkArticle: React.FC<GLinkArticleProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: 'GLinkArticle',
  });
  const classes = useUtilityClasses(props);
  const ownerState = {
    ...props
  }

  const Root = props.component ?? GLinkArticleRoot

  return (
    <Root className={classes.root} ownerState={ownerState}>
      <Link onClick={props.onClick}>
        <span>
          {props.label}
        </span>
      </Link>
    </Root>
  )
}


const GLinkArticleRoot = styled("div", {
  name: 'GLinkArticle',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
    ];
  },
})<{ ownerState: GLinkArticleProps }>(({ theme }) => {
  return {
    "span": {
      wordBreak: 'break-word'
    }
  };
});