import { darken, generateUtilityClass, lighten, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { OwnerState } from "./useOwnerState";
import { FsColors } from "../fs-theme";



const MUI_NAME = 'FsPanelProperties';

export interface FsPanelPropertiesClasses {
  root: string;
  propertyRow: string;
  propertyLabel: string;
  propertyValue: string;
  propertyList: string;
  propertyListItem: string;
  configOptionsListItem: string;
  label: string;
  commentList: string;
  commentItem: string;
  commentText: string;
  commentMeta: string;
  commentAuthor: string;
  commentDate: string;
  childRow: string;
  childRowIndented: string;
}

export type FsPanelPropertiesClassKey = keyof FsPanelPropertiesClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    propertyRow: ['propertyRow'],
    propertyLabel: ['propertyLabel'],
    propertyValue: ['propertyValue'],
    propertyList: ['propertyList'],
    propertyListItem: ['propertyListItem'],
    configOptionsListItem: ['configOptionsListItem'],
    label: ['label'],
    commentList: ['commentList'],
    commentItem: ['commentItem'],
    commentText: ['commentText'],
    commentMeta: ['commentMeta'],
    commentAuthor: ['commentAuthor'],
    commentDate: ['commentDate'],
    childRow: ['childRow'],
    childRowIndented: ['childRowIndented'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsPanelPropertiesRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({
  display: 'flex',
  flexDirection: 'column',
  width: '100%',
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
    width: '200px',
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

  [`& .${MUI_NAME}-configOptionsListItem`]: {
    fontWeight: 400,
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    backgroundColor: ownerState.isDarkMode ? darken(FsColors.semantic.info, 0.8) : lighten(FsColors.semantic.info, 0.5),
    border: `1px solid ${ownerState.isDarkMode ? darken(FsColors.semantic.info, 0.1) : darken(FsColors.semantic.info, 0.3)}`,
    paddingLeft: theme.spacing(0.625),
    paddingRight: theme.spacing(0.625),
  },

  [`& .${MUI_NAME}-label`]: {
    position: "relative",
    display: "inline-flex",
    alignItems: "center",
    height: 28,
    marginLeft: 12,
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

  [`& .${MUI_NAME}-commentList`]: {
    display: 'flex',
    flexDirection: 'column',
    gap: theme.spacing(1),
    width: '100%',
  },

  [`& .${MUI_NAME}-commentItem`]: {
    display: 'flex',
    flexDirection: 'column',
  },

  [`& .${MUI_NAME}-commentText`]: {
    ...theme.typography.subtitle2,
    fontWeight: 400,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },

  [`& .${MUI_NAME}-commentMeta`]: {
    display: 'flex',
    justifyContent: 'flex-end',
  },

  [`& .${MUI_NAME}-commentAuthor`]: {
    ...theme.typography.caption,
    color: ownerState.isDarkMode ? FsColors.dark.textMuted : FsColors.light.textSecondary,
    fontStyle: 'italic',
  },

  [`& .${MUI_NAME}-commentDate`]: {
    ...theme.typography.caption,
    color: ownerState.isDarkMode ? FsColors.dark.textMuted : FsColors.light.textSecondary,
  },

  [`& .${MUI_NAME}-childRow`]: {
    display: 'flex',
    alignItems: 'center',
    gap: theme.spacing(0.75),
  },

  [`& .${MUI_NAME}-childRowIndented`]: {
    display: 'flex',
    alignItems: 'center',
    gap: theme.spacing(0.75),
    paddingLeft: theme.spacing(3),
  },
}));
