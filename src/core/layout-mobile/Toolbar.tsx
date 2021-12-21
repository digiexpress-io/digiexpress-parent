import { styled, Toolbar, ToolbarProps } from '@mui/material';

interface StyledToolbarProps extends ToolbarProps {
  toolbarHeight: number;
}


const StyledToolbar = styled(Toolbar, {

})<StyledToolbarProps>(
  ({ toolbarHeight }) => ({
    height: toolbarHeight,
    flexShrink: 0,
    whiteSpace: 'nowrap',
    boxSizing: 'border-box',
  }),
);

export default StyledToolbar;
