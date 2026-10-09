import { generateUtilityClass, styled, alpha, Tooltip } from '@mui/material';
import { TooltipProps } from '@mui/material/Tooltip';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'FsDirentPrintoutPage';

export interface FsDirentPrintoutPageClasses {
  root: string;
  titleRow: string;
  title: string;
  formContainer: string;
  resourceList: string;
  dialogListItem: string;
  dialogListItemActive: string;
  dialogItemEnd: string;
  dialogCheckmark: string;
  dialogThumbnail: string;
  dialogThumbnailAnchor: string;
}

export type FsDirentPrintoutPageClassKey = keyof FsDirentPrintoutPageClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    titleRow: ['titleRow'],
    title: ['title'],
    formContainer: ['formContainer'],
    resourceList: ['resourceList'],
    dialogListItem: ['dialogListItem'],
    dialogListItemActive: ['dialogListItemActive'],
    dialogItemEnd: ['dialogItemEnd'],
    dialogCheckmark: ['dialogCheckmark'],
    dialogThumbnail: ['dialogThumbnail'],
    dialogThumbnailAnchor: ['dialogThumbnailAnchor'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

// Forwards emotion's generated className directly to Tooltip's classes.tooltip so
// the style is applied to the portalled tooltip div, not just the trigger wrapper.
export const FsDirentPrintoutPageThumbnailTooltip = styled(
  ({ className, ...props }: TooltipProps) => (
    <Tooltip {...props} classes={{ tooltip: className }} />
  )
)(() => ({
  backgroundColor: 'transparent',
  boxShadow: 'none',
  padding: 0,
  maxWidth: 'none',
}));

export const FsDirentPrintoutPageThumbnailPreview = styled('img', {
  name: MUI_NAME,
  slot: 'ThumbnailPreview',
})(() => ({
  maxWidth: 120,
  maxHeight: 120,
  objectFit: 'contain',
  display: 'block',
}));

export const FsDirentPrintoutPageRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',

  [`& .${MUI_NAME}-titleRow`]: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: theme.spacing(2),
  },

  [`& .${MUI_NAME}-title`]: {
    ...theme.typography.body1,
    color: FsColors.base.text,
    marginBottom: theme.spacing(2),
    fontWeight: 500
  },

  [`& .${MUI_NAME}-formContainer`]: {
    display: 'flex',
    flexDirection: 'column',
  },

  [`& .${MUI_NAME}-resourceList`]: {
    display: 'flex',
    flexDirection: 'column',
    gap: theme.spacing(0.5),
  },

}));

export const FsDirentPrintoutPageDialogList = styled('div', {
  name: MUI_NAME,
  slot: 'DialogList',
})(({ theme }) => ({
  [`& .${MUI_NAME}-dialogListItem`]: {
    paddingTop: theme.spacing(0.5),
    paddingBottom: theme.spacing(0.5),
    '& .MuiListItemText-primary': {
      fontWeight: 500,
    },
  },

  [`& .${MUI_NAME}-dialogListItemActive`]: {
    backgroundColor: alpha(FsColors.semantic.success, 0.12),
    border: `1px solid ${alpha(FsColors.semantic.success, 0.45)}`,
    borderRadius: theme.shape.borderRadius,
  },

  [`& .${MUI_NAME}-dialogItemEnd`]: {
    display: 'flex',
    alignItems: 'center',
    gap: theme.spacing(0.5),
    marginLeft: theme.spacing(1),
    flexShrink: 0,
  },

  [`& .${MUI_NAME}-dialogCheckmark`]: {
    fontSize: 18,
    color: FsColors.semantic.success,
    flexShrink: 0,
    marginRight: theme.spacing(0.5),
  },

  [`& .${MUI_NAME}-dialogThumbnail`]: {
    width: 32,
    height: 32,
    objectFit: 'cover',
    flexShrink: 0,
    borderRadius: 2,
    cursor: 'pointer',
  },

  [`& .${MUI_NAME}-dialogThumbnailAnchor`]: {
    display: 'flex',
    alignItems: 'center',
    flexShrink: 0,
  },

}));
