import React from 'react';
import * as colors from 'components-colors';

import { LayoutList, NavigationButton } from 'components-generic';
import PlaybooksCreateDialog from '../PlaybooksCreate';


const color_list_all = colors.cyan;
const color_selected = colors.steelblue;
const color_create = colors.cocktail_green;


const Playbooks: React.FC = () => {
  const [playbookCreateOpen, setPlaybookCreateOpen] = React.useState(false);

  function handlePlaybookCreate() {
    setPlaybookCreateOpen(prev => !prev);
  }

  function handlePlaybookListAll() {
    
  }
  function handlePlaybookSelected() {

  }

  const items: React.ReactNode[] = [];
  return (<LayoutList slots={{
    navigation: (<>
      <PlaybooksCreateDialog open={playbookCreateOpen} onClose={handlePlaybookCreate} />
      
      <NavigationButton id='libra.navButton.playbook.selected'
        values={{}}
        color={color_selected}
        active={false}
        onClick={handlePlaybookSelected} />
      <NavigationButton id='libra.navButton.playbook.list_all'
        values={{}}
        color={color_list_all}
        active={false}
        onClick={handlePlaybookListAll} />
      <NavigationButton id='libra.navButton.playbook.create'
        values={{}}
        color={color_create}
        active={false}
        onClick={handlePlaybookCreate} />
    </>),
    items,
    pagination: <></>
  }} />)
}

export { Playbooks };
