import { alpha, generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { GContractItemProps } from "./GContractItem";


export const MUI_NAME = 'GContracts';

export interface GContractsClasses {
  root: string;
  item: string;
  status: string;
  taskRefId: string;
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
    contractItem: ['contractItem'],
    status: ['status'],
    lastModified: ['lastModified'],
    taskRefId: ['taskRefId'],
    messages: ['messages'],
    files: ['files'],
    messagesCount: ['messagesCount'],
    filesCount: ['filesCount'],
    noValue: ['noValue'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

export const GContractItem = styled("div", {
  name: MUI_NAME,
  slot: 'ContractItem',
  overridesResolver: (_props, styles) => {
    return [
      styles.contractItem
    ];
  },
})<{ ownerState: GContractItemProps }>(({ theme, ownerState }) => {

  // const iconColor = ownerState.color ? ownerState.color : 'inherit';

  const color = theme.palette.info.main;

  return {

    '& .MuiGrid-item': {
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'center',
    },
    '& .GContracts-status': {
      fontWeight: 'bold',
      marginRight: theme.spacing(0.5)
    },
    '& .GContracts-taskRefId': {
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
      backgroundColor: `${alpha(color, 0.3)}`,
      color: theme.palette.text.primary
    },
    '& .GContracts-filesCount': {
      [theme.breakpoints.down('md')]: {
        height: '30px',
        width: '30px',
      },
      height: '35px',
      width: '35px',
      backgroundColor: `${alpha(color, 0.3)}`,
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
      color: color,
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
    borderBottom: `1px solid ${theme.palette.divider}`,

    '& .GSort-root': {
      display: 'flex',
      width: '100%',
      marginBottom: theme.spacing(1),
      [theme.breakpoints.down('sm')]: {
        justifyContent: 'center',
      },
      [theme.breakpoints.up('sm')]: {
        justifyContent: 'flex-end',
      },
    },    
  };
});