import { StyledButton, StyledButtonProps } from './StyledButton';
import { StyledTooltip } from './StyledTooltip';
import { StyledDialog, StyledDialogProps } from './StyledDialog';
import { StyledTab, StyledTabProps } from './StyledTab';
import { StyledAvatar, StyledAvatarProps } from './StyledAvatar';
import { StyledMarkdown, StyledMarkdownProps } from './StyledMarkdown';
import { StyledLink, StyledLinkProps } from './StyledLink';
import { StyledCard, StyledCardProps, cardStylesButtoned as cardStylesButtonedAs } from './StyledCard';

import { StyledSelect, SelectProps } from './StyledSelect';

declare namespace PortalStyles {
  export type { StyledMarkdownProps as MarkdownProps };
  export type { StyledLinkProps as LinkProps };
  export type { SelectProps };
  export type { StyledCardProps };
  export type { StyledButtonProps };
  export type { StyledDialogProps };
  export type { StyledAvatarProps };
  export type { StyledTabProps }
}

namespace PortalStyles {
  export const Button = StyledButton;
  export const Tooltip = StyledTooltip;
  export const Dialog = StyledDialog;
  export const Tab = StyledTab;
  export const Avatar = StyledAvatar;
  export const Markdown = StyledMarkdown;
  export const Link = StyledLink;
  export const Select = StyledSelect;
  export const Card = StyledCard;
  export const cardStylesButtoned = cardStylesButtonedAs;
}

export default PortalStyles;
