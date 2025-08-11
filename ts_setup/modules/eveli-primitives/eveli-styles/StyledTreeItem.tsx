import * as React from "react";
import { Typography, Box, Divider, styled, Badge } from "@mui/material";

import { TreeItem2, TreeItemProps, treeItemClasses } from "@mui/x-tree-view";
import { SvgIconProps } from "@mui/material/SvgIcon";


const StyledTreeItemRoot = styled(TreeItem2)(({ theme }) => ({
  [`& .${treeItemClasses.content}`]: {
    paddingRight: theme.spacing(1),
  },

  [`& .${treeItemClasses.groupTransition}`]: {
    marginLeft: 0,

    [`& .${treeItemClasses.content}`]: {
      paddingLeft: theme.spacing(1)
    }
  },

}));

type StyledTreeItemProps = TreeItemProps & {
  color?: string;
  labelcolor?: string;
  textcolor?: string;
  labelIcon?: React.ElementType<SvgIconProps>;
  labelButton?: React.ReactNode;
  labelInfo?: string | React.ReactNode;
  labelText: string | React.ReactNode;
};

const StyledTreeItem: React.FC<StyledTreeItemProps> = (props) => {
  const {
    labelButton,
    color,
    labelIcon: LabelIcon,
    labelInfo,
    labelText,
    ...other
  } = props;

  const labelTypeToShow = typeof labelInfo === "string" ? <Badge color='primary' badgeContent={labelInfo} /> : <>{labelInfo}</>;

  return (
    <StyledTreeItemRoot
      label={
        <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
          {labelButton ? labelButton : (
            LabelIcon ? (<Box component={LabelIcon} sx={{ mr: 1, fontSize: 'medium' }} />) : null
          )}
          <Typography noWrap={true} fontWeight='bold' pr={1}>
            {labelText}
          </Typography>
          <Box sx={{ marginLeft: 1, display: 'flex', alignItems: 'center' }}>{labelTypeToShow}</Box>
        </Box>
      }

      {...other}
    />
  );
}

const StyledTreeItemOption: React.FC<{
  labelText: React.ReactNode;
  nodeId: string;
  color: string;
  icon?: React.ElementType<SvgIconProps>;
  onClick: () => void
}> = (props) => {

  return (<>
    <StyledTreeItemRoot
      onClick={props.onClick}
      itemId={props.nodeId}

      label={
        <Box sx={{ display: "flex", alignItems: "center", p: 0.2, pr: 0 }} >
          <Typography variant="body2" sx={{ fontWeight: "inherit", flexGrow: 1 }}>
            {props.labelText}
          </Typography>
        </Box>
      }
    />
    <Divider />
  </>
  );
}


export type { StyledTreeItemProps };
export { StyledTreeItem, StyledTreeItemRoot, StyledTreeItemOption };