import { Search } from './Search';

/*
  const openAsset = (model: Hdes.ModelAPI.Model) => actions.handleTabAdd({id: model.id, label: model.name});

  const list = models.map(model => (
    <ListItem key={model.id} button className={classes.nested} onClick={() => openAsset(model)}>
      <ListItemText secondary={model.name} />
    </ListItem>));
*/

import { Layout } from '../deps';

const createSearch = (layout: Layout.Session.ContextType) => {
  layout.actions.handleTabAdd({ id: 'search', label: 'Search...' });
  return {};
};

export { Search, createSearch };
