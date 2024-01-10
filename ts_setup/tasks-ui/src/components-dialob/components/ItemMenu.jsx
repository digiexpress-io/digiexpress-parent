import React from 'react';
import {Dropdown, Label} from 'semantic-ui-react';
import ItemTypeMenu from './ItemTypeMenu';
import {connect} from 'react-redux';
import {addItem, showItemOptions, copyItem} from '../actions';
import PropTypes from 'prop-types';

class ItemMenu extends React.PureComponent {

  static get propTypes() {
    return {
      item: PropTypes.object.isRequired,
      parentItemId: PropTypes.string.isRequired,
      onDelete: PropTypes.func.isRequired
    };
  }

  constructor(props) {
    super(props);
    this.showItemOptions = () => this.props.showItemOptions(this.props.item.get('id'));
    this.onItemSelect = (config) => this.props.addItem(config, this.props.parentItemId, this.props.item.get('id'));
    this.copyItem = () => this.props.copyItem(this.props.item.get('id'));
  }

  render() {
    return (
      <Dropdown icon='content' lazyLoad direction='left'>
        <Dropdown.Menu>
          <Dropdown.Item icon='options' text='Options...' onClick={this.showItemOptions}/>
          <Dropdown.Item disabled={!!this.props.formTag} icon='remove' text='Delete' onClick={this.props.onDelete} />
          <Dropdown.Item disabled={!!this.props.formTag} icon='copy' text='Duplicate' onClick={this.copyItem} />
          <Dropdown.Divider />
          <Dropdown item text='Insert new'>
              <Dropdown.Menu>
                <ItemTypeMenu onSelect={this.onItemSelect} />
              </Dropdown.Menu>
          </Dropdown>
        </Dropdown.Menu>
      </Dropdown>
    );
  }
}

const ItemMenuConnected = connect(
  state => ({
    formTag: state.dialobComposer.form.get('_tag')
  }),
  {
    addItem,
    showItemOptions,
    copyItem
  }
)(ItemMenu);

export {
  ItemMenu,
  ItemMenuConnected as default
}
