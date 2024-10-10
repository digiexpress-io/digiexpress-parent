import * as React from "react";
import { styled } from "@mui/material/styles";
import { Typography, Box, useTheme} from "@mui/material";
import TreeItem, { TreeItemProps, treeItemClasses } from "@mui/lab/TreeItem";
import { SvgIconProps } from "@mui/material/SvgIcon";


const StyledTreeItemRoot = styled(TreeItem)(({ theme }) => ({
  color: `var(--tree-view-text-color, ${theme.palette.text.secondary})`,
  [`& .${treeItemClasses.content}`]: {
    color: `var(--tree-view-text-color, ${theme.palette.explorerItem.main})`,
    borderTopRightRadius: theme.spacing(2),
    borderBottomRightRadius: theme.spacing(2),
    paddingRight: theme.spacing(1),
    fontWeight: theme.typography.fontWeightMedium,
    "&.Mui-expanded": {
      fontWeight: theme.typography.fontWeightRegular
    },
    "&:hover": {
      backgroundColor: `var(--tree-view-hover-color, ${theme.palette.action.hover})`,
    },
    "&.Mui-focused, &.Mui-selected, &.Mui-selected.Mui-focused": {
      backgroundColor: `var(--tree-view-bg-color, ${theme.palette.action.selected})`,
      color: `var(--tree-view-color, ${theme.palette.explorerItem.dark})` //"var(--tree-view-color)"
    },
    [`& .${treeItemClasses.label}`]: {
      //fontWeight: "inherit",
      color: "inherit"
    }
  },
  [`& .${treeItemClasses.group}`]: {
    marginLeft: 0,
    [`& .${treeItemClasses.content}`]: {
      paddingLeft: theme.spacing(2)
    }
  }
}));

type StyledTreeItemProps = TreeItemProps & {
  color?: string;
  bgcolor?: string;
  labelcolor?: string;
  hovercolor?: string;
  textcolor?: string;
  labelIcon?: React.ElementType<SvgIconProps>;
  labelButton?: React.ReactChild;
  labelInfo?: string | React.ReactChild;
  labelText: string | React.ReactChild;
};

const StyledTreeItem: React.FC<StyledTreeItemProps> = (props) => {
  const theme = useTheme();
  const {
    labelButton,
    labelcolor,
    color,
    bgcolor,
    hovercolor,
    textcolor,
    labelIcon: LabelIcon,
    labelInfo,
    labelText,
    ...other
  } = props;

  let resolvedLabelcolor = "inherit";
  if(labelcolor) {
    resolvedLabelcolor = theme.palette[labelcolor]?.main;
    if(!resolvedLabelcolor && labelcolor.indexOf(".") > -1) {
      const coolors = labelcolor.split(".");
      resolvedLabelcolor = theme.palette[coolors[0]][coolors[1]];
    }
  }

  return (
    <StyledTreeItemRoot
      sx={{ backgroundColor: "explorer.main" }}
      label={
        <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
          {labelButton ? labelButton : <Box component={LabelIcon}
            sx={{
              mr: 1,
              color: resolvedLabelcolor,
            }} />}
          <Typography noWrap={true} maxWidth="300px"
            variant="body2"
            sx={{ flexGrow: 1 }}
          >
            {labelText}
          </Typography>
          <Typography variant="caption" color="inherit" >
            {labelInfo}
          </Typography>
        </Box>
      }

      {...other}
      style={{
        '--tree-view-text-color': textcolor ? theme.palette[textcolor].main : textcolor,
        '--tree-view-color': color ? theme.palette[color].main : color,
        '--tree-view-bg-color': bgcolor ? theme.palette[bgcolor].main : bgcolor,
        '--tree-view-hover-color': hovercolor ? theme.palette[hovercolor].main : hovercolor,
      }}
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
  const theme = useTheme();

  return (
    <StyledTreeItemRoot
      onClick={props.onClick}
      nodeId={props.nodeId}

      label={
        <Box sx={{ display: "flex", alignItems: "center", p: 0.2, pr: 0}} >
          <Box component={props.icon}  color={theme.palette[props.color].main} sx={{ pl: 1, mr: 1}} />
          <Typography 
            variant="body2"
            sx={{ fontWeight: "inherit", flexGrow: 1 }}
          >
            {props.labelText}
          </Typography>
        </Box>
      }
    />
  );
}


export type { StyledTreeItemProps };
export { StyledTreeItem, StyledTreeItemRoot, StyledTreeItemOption };