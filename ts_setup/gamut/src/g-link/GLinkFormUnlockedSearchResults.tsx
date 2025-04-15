import React from 'react';
import { Typography, styled, useThemeProps, generateUtilityClass, Link } from '@mui/material';
import CircleIcon from '@mui/icons-material/Circle';
import composeClasses from '@mui/utils/composeClasses';
import { GOverridableComponent } from '../g-override';


const MUI_NAME = 'GLinkFormUnlockedSearchResults';


export interface GLinkFormUnlockedSearchResultsClasses {
  root: string
}

export type GLinkFormUnlockedSearchResultsClassKey = keyof GLinkFormUnlockedSearchResultsClasses;

export interface GLinkFormUnlockedSearchResultsProps {
  label: string; // topic name
  value: string; // link name - locale based
  onClick: () => void;
  component?: GOverridableComponent<GLinkFormUnlockedSearchResultsProps>;
  slots?: {
    link?: React.ElementType<Omit<GLinkFormUnlockedSearchResultsProps, 'component' | 'slots'>>,
  };
}

const useUtilityClasses = (ownerState: GLinkFormUnlockedSearchResultsProps) => {
  const slots = { root: ['root'] };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

const GLinkFormUnlockedSearchResultsRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
    ];
  },
})<{ ownerState: GLinkFormUnlockedSearchResultsProps }>(({ theme }) => {
  return {
    "span": {
      display: 'flex',
      alignItems: 'center'
    },
    "& .MuiSvgIcon-root": {
      marginRight: theme.spacing(1),
      fontSize: '20px'
    }
  }
});

export const GLinkFormUnlockedSearchResults: React.FC<GLinkFormUnlockedSearchResultsProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);
  const ownerState = {
    ...props
  }
  const Root = props.component ?? GLinkFormUnlockedSearchResultsRoot
  const LinkSlot = props.slots?.link ? props.slots?.link : () => (<>
        <Link onClick={props.onClick}>
          <span>
            <CircleIcon color='info' sx={{ height: '10px', width: '10px' }} />
            <Typography>{props.label}</Typography>
          </span>
        </Link>
    </>);
  return (
    <Root ownerState={ownerState} className={classes.root}>
      <LinkSlot {...props}/>
    </Root>
  )
}
