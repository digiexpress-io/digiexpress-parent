import React from 'react';
import {Table, Input, Icon} from 'semantic-ui-react';
import Item, {connectItem} from './Item';
import ItemMenu from '../components/ItemMenu';
import Validations from '../components/Validations';
import Scrolltarget from './Scrolltarget';
import ConvertItem from '../components/ConvertItem';

class SimpleField extends Item {
  render() {
    return (
      <Scrolltarget itemId={this.props.itemId} className='composer-scrolltarget'>
        <Table onClick={(e) => {e.stopPropagation(); this.setActive(true);}}  attached={'top'} color={this.getBorderColor()}>
          <Table.Body>
            <Table.Row>
              <Table.Cell selectable collapsing width={2} >
                <a onClick={() => {if (this.props.editable) { this.changeId(); }}}>{this.props.itemId}</a>
              </Table.Cell>
              <Table.Cell>
                <Input transparent fluid placeholder={this.props.placeholder} value={this.props.item.getIn(['label', this.props.language]) || ''} onChange={(e) => this.setAttribute('label', e.target.value, this.props.language)}/>
              </Table.Cell>
              <Table.Cell collapsing>
                <ConvertItem itemType={this.props.item.get('type')} viewType={this.props.item.get('view')} itemId={this.props.itemId}/>
              </Table.Cell>
              <Table.Cell collapsing>
                <ItemMenu item={this.props.item} parentItemId={this.props.parentItemId} onDelete={this.deleteItem}/>
              </Table.Cell>
            </Table.Row>
          </Table.Body>
        </Table>

          <Table onClick={(e) => {e.stopPropagation(); this.setActive(true);}} celled attached={this.props.active ? true : 'bottom'} >
            <Table.Body>
              <Table.Row>
                <Table.Cell error={this.getErrors().filter(e => e.get('type') === 'VISIBILITY').size > 0}>
                  <div className='dialob-rule'>
                    <Icon name='eye' className='dialob-rule-icon' />
                    {this.props.item.get('activeWhen') || <span className='dialob-placeholder'>Visibility</span>}
                  </div>
                </Table.Cell>
              </Table.Row>
              {
                this.props.active &&
                  <React.Fragment>
                    <Table.Row>
                      <Table.Cell error={this.getErrors().filter(e => e.get('type') === 'REQUIREMENT').size > 0}>
                        <div className='dialob-rule'>
                          <Icon name='gavel' className='dialob-rule-icon' />
                          {this.props.item.get('required') || <span className='dialob-placeholder'>Requirement</span>}
                        </div>
                      </Table.Cell>
                    </Table.Row>
                    <Table.Row>
                      <Table.Cell error={this.getErrors().filter(e => e.get('type') === 'GENERAL' && e.get('message') === 'INVALID_DEFAULT_VALUE').size > 0}>
                        <Input icon='pencil' transparent fluid placeholder='Default value' value={this.props.item.get('defaultValue') || ''} onChange={(e) => this.setAttribute('defaultValue', e.target.value)}/>
                      </Table.Cell>
                    </Table.Row>
                  </React.Fragment>
               }
            </Table.Body>
        </Table>
        {
          this.props.active && <Validations item={this.props.item} validations={this.props.validations} readOnly={!this.props.editable} />
        }
      </Scrolltarget>
    );
  }
}

const SimpleFieldConnected = connectItem(SimpleField);

export {
  SimpleField,
  SimpleFieldConnected as default
};
