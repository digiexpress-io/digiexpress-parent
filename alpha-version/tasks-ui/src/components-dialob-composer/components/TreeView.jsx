import React, {Component} from 'react';
import {connect} from 'react-redux';
import {Menu, List} from 'semantic-ui-react';
import {TreeItem} from '../items';
import {moveItem} from '../actions';

class TreeView extends Component {

  constructor(props) {
     super(props);
     this.doMoveItem = this.moveItem.bind(this);
     this.doGetItemById = this.getItemById.bind(this);
  }

  moveItem(dragIndex, hoverIndex, dragParent, hoverParent, itemId) {
    this.props.moveItem(dragIndex, hoverIndex, dragParent, hoverParent, itemId);
  }

  getItemById(itemId) {
    return this.props.items.get(itemId);
  }

  render() {
    const rootItem = this.props.items.get(this.props.rootItemId);
    const parent = rootItem;
    const treeItems = rootItem && rootItem.get('items') && rootItem.get('items')
            .map(itemId => this.getItemById(itemId))
            .map((page, index) => <TreeItem treeCollapsible={true} index={index} parent={parent} isPage={true} id={page.get('id')} moveItem={this.doMoveItem} getItemById={this.doGetItemById} key={page.get('id')} itemId={page.get('id')} icon='folder' pageId={page.get('id')}/>);
    return (
      <Menu vertical>
        <Menu.Item>
          <List size='small'>
             {treeItems}
          </List>
        </Menu.Item>
      </Menu>);
  }
}

const TreeViewConnected = connect(
  state => ({
    items: state.dialobComposer.form && state.dialobComposer.form.get('data'),
    rootItemId: state.dialobComposer.editor.get('rootItemId')
  }),
  {
    moveItem
  }
)(TreeView);

export {
  TreeViewConnected as default,
  TreeView
};

