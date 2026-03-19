import { generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { OwnerState } from "./useOwnerState";
import { FsColors } from "../fs-theme";



const MUI_NAME = 'FsProperties';

export interface FsPropertiesClasses {
  root: string;
  propertyRow: string;
  propertyLabel: string;
  propertyValue: string;
  propertyList: string;
  propertyListItem: string;
  tagLabel: string;
}

export type FsPropertiesClassKey = keyof FsPropertiesClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    propertyRow: ['propertyRow'],
    propertyLabel: ['propertyLabel'],
    propertyValue: ['propertyValue'],
    propertyList: ['propertyList'],
    propertyListItem: ['propertyListItem'],
    tagLabel: ['tagLabel'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsPropertiesRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({
  display: 'flex',
  flexDirection: 'column',
  border: `1px solid ${ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border}`,

  [`& > .${MUI_NAME}-propertyRow:nth-of-type(odd)`]: {
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
  },

  [`& .${MUI_NAME}-propertyRow`]: {
    display: 'flex',
    gap: theme.spacing(2),
    padding: theme.spacing(1, 1.5),
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.background,
    borderBottom: `1px solid ${ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
    '&:last-child': {
      borderBottom: 'none'
    },
  },

  [`& .${MUI_NAME}-propertyLabel`]: {
    ...theme.typography.subtitle2,
    alignContent: 'center',
    fontWeight: 500,
    textTransform: 'uppercase',
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    width: '230px',
    flexShrink: 0,
    paddingRight: theme.spacing(1.875),
  },

  [`& .${MUI_NAME}-propertyValue`]: {
    ...theme.typography.subtitle2,
    fontWeight: 400,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    flex: 1,
  },

  [`& .${MUI_NAME}-propertyList`]: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: theme.spacing(0.75),
    flex: 1,
  },

  [`& .${MUI_NAME}-propertyListItem`]: {
    fontWeight: 400,
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
    borderColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
    border: '1px solid',
    paddingLeft: theme.spacing(0.625),
    paddingRight: theme.spacing(0.625),
  },

  [`& .${MUI_NAME}-tagLabel`]: {
    position: "relative",
    display: "inline-flex",
    alignItems: "center",
    height: 28,
    padding: "0 12px 0 16px",
    background: theme.palette.primary.main,
    color: theme.palette.primary.contrastText,
    fontSize: 13,
    borderRadius: 0,

    // left triangle (arrow)
    "&::before": {
      content: '""',
      position: "absolute",
      left: -12,
      width: 0,
      height: 0,
      borderTop: "14px solid transparent",
      borderBottom: "14px solid transparent",
      borderRight: `12px solid ${theme.palette.primary.main}`,
    },

    // hole
    "&::after": {
      content: '""',
      position: "absolute",
      left: -6,
      width: 6,
      height: 6,
      borderRadius: "50%",
      background: theme.palette.background.paper,
    },

    '& .MuiTypography-root': {
      fontSize: "inherit",
      fontWeight: "inherit",
    },
  },
}));