import { alpha, generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { GContractItemProps } from "./GContractItem";


export const MUI_NAME = 'GContracts';

export interface GContractsClasses {
  root: string;
  item: string;
  status: string;
  lastModified: string;
  messages: string;
  files: string;
  messagesCount: string;
  filesCount: string;
  noValue: string;
}
export type GContractsClassKey = keyof GContractsClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    item: ['item'],
    status: ['status'],
    lastModified: ['lastModified'],
    messages: ['messages'],
    files: ['files'],
    messagesCount: ['messagesCount'],
    filesCount: ['filesCount'],
    noValue: ['noValue'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

export const GContractItemRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Item',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.status,
      styles.lastModified,
      styles.messages,
      styles.files,
      styles.messagesCount,
      styles.filesCount,
      styles.noValue
    ];
  },
})<{ ownerState: GContractItemProps }>(({ theme, ownerState }) => {

  const iconColor = ownerState.color ? ownerState.color : 'inherit';

  return {
    cursor: 'pointer',

    '& .MuiGrid-item': {
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'center'
    },
    '& .GContracts-status': {
      fontWeight: 'bold',
      marginRight: theme.spacing(0.5)
    },
    '& .GContracts-noValue': {
      [theme.breakpoints.down('md')]: {
        height: '30px',
        width: '30px',
      },
      height: '35px',
      width: '35px',
      backgroundColor: 'unset',
      color: theme.palette.text.primary
    },
    '& .GContracts-messagesCount': {
      [theme.breakpoints.down('md')]: {
        height: '30px',
        width: '30px',
      },
      height: '35px',
      width: '35px',
      backgroundColor: `${alpha(iconColor, 0.3)}`,
      color: theme.palette.text.primary
    },
    '& .GContracts-filesCount': {
      [theme.breakpoints.down('md')]: {
        height: '30px',
        width: '30px',
      },
      height: '35px',
      width: '35px',
      backgroundColor: `${alpha(iconColor, 0.3)}`,
      color: theme.palette.text.primary
    },
    '& .GContracts-lastModified': {
      fontWeight: 'bold',
      marginRight: theme.spacing(0.5)
    },
    '& .GContracts-messages': {
      fontWeight: 'bold',
      marginRight: theme.spacing(0.5),
    },
    '& .GContracts-files': {
      fontWeight: 'bold',
      marginRight: theme.spacing(0.5),
    },
    '& .GContracts-offerIcon': {
      color: ownerState.color,
      fontSize: 'large'
    }
  };
});


export const GContractsRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },
})(({ theme }) => {
  return {

  };
});