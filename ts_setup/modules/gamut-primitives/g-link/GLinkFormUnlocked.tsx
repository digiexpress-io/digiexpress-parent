import React from 'react';
import { Typography, styled, useThemeProps, generateUtilityClass, Link } from '@mui/material';
import ForwardIcon from '@mui/icons-material/Forward';
import composeClasses from '@mui/utils/composeClasses';
import { GOverridableComponent } from '@dxs-ts/gamut-api';


const MUI_NAME = 'GLinkFormUnlocked';


export interface GLinkFormUnlockedClasses {
  root: string
}

export type GLinkFormUnlockedClassKey = keyof GLinkFormUnlockedClasses;

export interface GLinkFormUnlockedProps {
  label: string; // topic name
  value: string; // link name - locale based
  onClick: () => void;
  component?: GOverridableComponent<GLinkFormUnlockedProps>;
  slots?: {
    link?: React.ElementType<Omit<GLinkFormUnlockedProps, 'component' | 'slots'>>,
  };
}

const useUtilityClasses = (ownerState: GLinkFormUnlockedProps) => {
  const slots = { root: ['root'] };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

//TODO Perhaps this can be deleted, but let's wait and see
export const GLinkFormUnlocked: React.FC<GLinkFormUnlockedProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);
  const ownerState = {
    ...props
  }
  const Root = props.component ?? GLinkFormUnlockedRoot
  const LinkSlot = props.slots?.link ?? GLinkFormUnlockedSlot
  return (
    <Root ownerState={ownerState} className={classes.root}>
      <LinkSlot {...props}/>
    </Root>
  )
}

export const GLinkFormUnlockedSlot: React.FC<GLinkFormUnlockedProps> = (props) => {

  return (
    <Link onClick={props.onClick}>
      <span>
        <ForwardIcon color='info' />
        <Typography>{props.label}</Typography>
      </span>
    </Link>
  );
}

const GLinkFormUnlockedRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
    ];
  },
})<{ ownerState: GLinkFormUnlockedProps }>(({ theme }) => {
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