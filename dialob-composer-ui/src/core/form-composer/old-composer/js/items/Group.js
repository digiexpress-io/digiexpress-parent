import React from 'react';
import {Input, Segment, Table, Dropdown, Icon} from 'semantic-ui-react';
import Item, {connectItem} from './Item';
import ItemTypeMenu from '../components/ItemTypeMenu';
import ItemMenu from '../components/ItemMenu';
import classnames from 'classnames';
import Scrolltarget from './Scrolltarget';
import ConvertItem from '../components/ConvertItem';

class Group extends Item {

  render() {
    const {treeCollapsed, item, itemId, editable} = this.props;
    const itemTypeFilter = i => item.get('type') !== 'surveygroup' ? i.config.type !== 'survey' : true;

    return (
      <Scrolltarget itemId={this.props.itemId} className='composer-scrolltarget'>
        <Table attached={treeCollapsed ? null :'top'} onClick={(e) => {e.stopPropagation(); this.setActive(true);}} color={this.getBorderColor()}>
          <Table.Body>
            <Table.Row>
              <Table.Cell collapsing>
                <Icon name={treeCollapsed ? 'caret right' : 'caret down'} size='large' fitted onClick={() => this.setTreeCollapsed(!treeCollapsed)}/>
              </Table.Cell>
              <Table.Cell selectable collapsing width={2}>
                <a onClick={() => {if (editable) { this.changeId(); }}}>{itemId}</a>
              </Table.Cell>
              <Table.Cell>
                <Input transparent fluid placeholder={this.props.placeholder} value={item.getIn(['label', this.props.language]) || ''} onChange={(e) => this.setAttribute('label', e.target.value, this.props.language)}/>
              </Table.Cell>
              <Table.Cell collapsing>
                <ConvertItem itemType={this.props.item.get('type')} viewType={this.props.item.get('view')} itemId={this.props.itemId}/>
              </Table.Cell>
              <Table.Cell collapsing>
                <ItemMenu item={item} parentItemId={this.props.parentItemId} onDelete={this.deleteItem}/>
              </Table.Cell>
            </Table.Row>
          </Table.Body>
        </Table>

        { !treeCollapsed ?
         <React.Fragment>
            <Table onClick={(e) => {e.stopPropagation(); this.setActive(true);}}  celled attached >
              <Table.Body>
                <Table.Row>
                  <Table.Cell error={this.getErrors().filter(e => e.get('type') === 'VISIBILITY').size > 0}>
                    <div className='dialob-rule'>
                      <Icon name='eye' className='dialob-rule-icon' />
                      {this.props.item.get('activeWhen') || <span className='dialob-placeholder'>Visibility</span>}
                    </div>
                  </Table.Cell>
                </Table.Row>
              </Table.Body>
            </Table>
            <Segment onClick={(e) => {e.stopPropagation(); this.setActive(true);}}  className={classnames('composer-group', {'composer-active': this.props.active})} attached='bottom'>
              {this.createChildren({parentItemId: itemId, getItemById: this.props.getItemById})}

              <Dropdown button text='Add item' disabled={!editable} lazyLoad>
                <Dropdown.Menu>
                  <ItemTypeMenu itemTypeFilter={itemTypeFilter} onSelect={(config) => this.newItem(config, itemId)}/>
                </Dropdown.Menu>
              </Dropdown>
            </Segment>
        </React.Fragment>
        : null
        }
      </Scrolltarget>);
  }
}

const GroupConnected = connectItem(Group);

export {
  Group,
  GroupConnected as default
};
