import React, {Component} from 'react';
import {Modal, Button, Tab} from 'semantic-ui-react';
import {connect} from 'react-redux';
import {hideItemOptions} from '../actions';
import Styleclasses from './options/Styleclasses';
import Description from './options/Description';
import Choices from './options/Choices';
import ItemProps from './options/ItemProps';
import {CHOICE_ITEM_TYPES} from '../defaults';
import {findItemTypeConfig} from '../helpers/utils';

class ItemOptionsDialog extends Component {

  render() {
    if (this.props.itemOptions) {
      const item = this.props.getItem();
      let tabs = [
        {menuItem: 'Description', render: () => <Tab.Pane><Description item={item} /></Tab.Pane>},
        {menuItem: 'Style classes', render: () => <Tab.Pane><Styleclasses item={item} /></Tab.Pane>},
        {menuItem: 'Properties', render: () => <Tab.Pane><ItemProps item={item} /></Tab.Pane>},
      ];

      if (CHOICE_ITEM_TYPES.findIndex(t => t === item.get('type')) > -1) {
        tabs.unshift({menuItem: 'Choices', render: () => <Tab.Pane><Choices item={item} /></Tab.Pane>});
      }

      const itemTypeConfig = findItemTypeConfig(this.props.itemTypes, item.get('view') || item.get('type'));
      if (itemTypeConfig && itemTypeConfig.optionEditors) {
        itemTypeConfig.optionEditors.forEach(e => {
             tabs.push({menuItem: e.name, render: () => <Tab.Pane><e.editor item={item} /></Tab.Pane>});
          }
        );
      }

      return (
        <Modal open>
          <Modal.Header>Item options for <em>{item.get('id')}</em></Modal.Header>
          <Modal.Content scrolling>
            <Tab panes={tabs} />
          </Modal.Content>
          <Modal.Actions>
            <Button primary onClick={() => this.props.hideItemOptions()}>OK</Button>
          </Modal.Actions>
        </Modal>
      );
    } else {
      return null;
    }
  }
}

const ItemOptionsDialogConnected = connect(
  state => ({
    itemOptions: state.dialobComposer.editor && state.dialobComposer.editor.get('itemOptions'),
    itemTypes:  state.dialobComposer.config.itemTypes,
    get getItem() { return () => state.dialobComposer.form && state.dialobComposer.form.getIn(['data', this.itemOptions.get('itemId')]); }
  }), {
    hideItemOptions
  }
)(ItemOptionsDialog);

export {
  ItemOptionsDialogConnected as default,
  ItemOptionsDialog
};
