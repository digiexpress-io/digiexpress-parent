import React from 'react';
import { Box, Typography } from '@mui/material';
import TreeView from "@mui/lab/TreeView";
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";
import ArrowRightIcon from "@mui/icons-material/ArrowRight";


import { Composer, StencilClient } from '../../context';
import { LinkEdit } from '../../link/LinkEdit';
import LinkItem from './LinkItem';


const findMainId = (values: string[]) => {
  const result = values.filter(id => !id.endsWith("-nested"));
  if (result.length) {
    return result[0];
  }
  return undefined;
}


const LinkExplorer: React.FC<{ searchString: string }> = ({ searchString }) => {
  const { session } = Composer.useComposer();
  const [expanded, setExpanded] = React.useState<string[]>([]);
  const [editLink, setEditLink] = React.useState<undefined | StencilClient.LinkId>(undefined);

  const links: Composer.LinkView[] = React.useMemo(() => {
    if (searchString) {
      return session.search.filterLinks(searchString).map(searchResult => session.getLinkView(searchResult.source.id))
    }
    return session.links;
  }, [searchString, session]);


  return (
    <Box>
      {editLink ? <LinkEdit linkId={editLink} onClose={() => setEditLink(undefined)} /> : undefined}

      <Typography
        sx={{
          fontVariant: 'all-petite-caps',
          fontWeight: 'bold',
          color: 'explorerItem.main',
          ml: 1, mr: 1, mb: 1,
          borderBottom: '1px solid',
        }}>
      </Typography>

      <TreeView expanded={expanded}
        defaultCollapseIcon={<ArrowDropDownIcon />}
        defaultExpandIcon={<ArrowRightIcon />}
        defaultEndIcon={<div style={{ width: 24 }} />}
        onNodeToggle={(_event: React.SyntheticEvent, nodeIds: string[]) => {
          const active = findMainId(expanded);
          const newId = findMainId(nodeIds.filter(n => n !== active));
          if (active !== newId && active && newId) {
            nodeIds.splice(nodeIds.indexOf(active), 1);
          }
          setExpanded(nodeIds);
        }}>
        {links
          .map((w) => ({ w, name: session.getLinkName(w.link.id)?.name }))
          .sort((a, b) => a.name.localeCompare(b.name))
          .map((w) => w.w)
          .map((view) => (
            <LinkItem key={view.link.id} linkId={view.link.id} />
          ))}
      </TreeView>
    </Box>
  );
}

export { LinkExplorer }

