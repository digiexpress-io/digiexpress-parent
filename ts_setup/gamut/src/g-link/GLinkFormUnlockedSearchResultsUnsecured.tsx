import React from 'react';
import {
  Typography,
  styled,
  useThemeProps,
  generateUtilityClass,
  Link
} from '@mui/material';
import ForwardIcon from '@mui/icons-material/Forward';
import LockIcon from '@mui/icons-material/Lock';
import composeClasses from '@mui/utils/composeClasses';
import { GOverridableComponent } from '../g-override';
import { IamApi } from '../api-iam/iam-types';

const MUI_NAME = 'GLinkFormUnlockedSearchResults';

export interface GLinkFormUnlockedSearchResultsClasses {
  root: string;
}

export type GLinkFormUnlockedSearchResultsClassKey = keyof GLinkFormUnlockedSearchResultsClasses;

export interface GLinkFormUnlockedSearchResultsProps {
  label: string;
  value: string;
  onClick: () => void;
  formLinkAuthType: IamApi.FormLinkAuthType;
  component?: GOverridableComponent<GLinkFormUnlockedSearchResultsProps>;
  slots?: {
    link?: React.ElementType<Omit<GLinkFormUnlockedSearchResultsProps, 'component' | 'slots'>>;
  };
}

const useUtilityClasses = (ownerState: GLinkFormUnlockedSearchResultsProps) => {
  const slots = { root: ['root'] };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

const GLinkFormUnlockedSearchResultsRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => [styles.root],
})<{ ownerState: GLinkFormUnlockedSearchResultsProps }>(({ theme }) => ({
  'span': {
    display: 'flex',
    alignItems: 'center',
  },
  '& .MuiSvgIcon-root': {
    marginRight: theme.spacing(1),
    fontSize: '20px',
  },
}));

const getFormLinkIcon = (authType: IamApi.FormLinkAuthType) => {
  switch (authType) {
    case 'IS_ANON_FORM_ENABLED':
    case 'IS_USER_FORM_ENABLED':
    case 'IS_REP_ENABLED':
      return <ForwardIcon color="info" />;
    default:
      return <LockIcon color="error" />;
  }
};

export const GLinkFormUnlockedSearchResults: React.FC<GLinkFormUnlockedSearchResultsProps> = (initProps) => {
  const props = useThemeProps({ props: initProps, name: MUI_NAME });
  const classes = useUtilityClasses(props);
  const ownerState = { ...props };
  const Root = props.component ?? GLinkFormUnlockedSearchResultsRoot;

  const LinkSlot = props.slots?.link ? props.slots.link : () => (
    <Link onClick={props.onClick}>
      <span>
        {getFormLinkIcon(props.formLinkAuthType)}
        <Typography>{props.label}</Typography>
      </span>
    </Link>
  );

  return (
    <Root ownerState={ownerState} className={classes.root}>
      <LinkSlot {...props} />
    </Root>
  );
};
