import React from 'react';
import {Dropdown} from 'semantic-ui-react';
import {connect} from 'react-redux';
import {findItemTypeConfig} from '../helpers/utils';
import {changeItemType} from '../actions';

class ConvertItem extends React.PureComponent {

  constructor(props) {
    super(props);
    this.onSelect = this.selectType.bind(this);
  }

  selectType(_, data) {
    const option = data.options.find(o => o.value === data.value);
    this.props.changeItemType(option.config, this.props.itemId);
  }

  render() {
    const {itemTypes, viewType, itemType} = this.props;
    const thisItemType = findItemTypeConfig(itemTypes, viewType || itemType);
    const options = [
      {
        key: '_',
        text: 'Convert to:',
        disabled: true
      }
    ];

    if (thisItemType && thisItemType.convertible) {
      thisItemType.convertible.forEach(t => {
        const toItemType = findItemTypeConfig(itemTypes, t);
        if (toItemType) {
          options.push({
            key: toItemType.config.view || toItemType.config.type,
            text: toItemType.title,
            value: toItemType.config.view || toItemType.config.type,
            config: toItemType
          });
        }
      });
    }

    return (
      <Dropdown disabled={options.length === 1}
                trigger={<span>{thisItemType ? thisItemType.title : (viewType || itemType)}</span>}
                options={options}
                onChange={this.onSelect}
                />
    );

  }
}

const ConvertItemConnected = connect(
  state => ({
    itemTypes:  state.dialobComposer.config.itemTypes
  }), {
    changeItemType
  }
)(ConvertItem);

export {
  ConvertItemConnected as default,
  ConvertItem
};
