import React, {Component} from 'react';
import {List} from 'semantic-ui-react';
import {connect} from 'react-redux';
import {setActiveItem, showVariables, showValueSets} from '../actions';
import { translateErrorType, translateErrorMessage } from '../helpers/utils';

class ErrorList extends Component {

  translateError(error) {
    const type = translateErrorType(error);
    const text = translateErrorMessage(error);
    return (
      <React.Fragment>
        {type && <strong>{type}: </strong>}
        {text}
      </React.Fragment>
    );
  }

  resolveItemId(error) {

    if (error.get(0).get('type') && error.get(0).get('type').startsWith('VALUESET')) {
      const [valueSetId] = error.get(0).get('itemId').split(':', 2);
      const item = this.props.items.find(i => i.get('valueSetId') === valueSetId);
      if (item) { 
        return item.get('id');
      } else {
        return null;
      }
    }
    return error.get(0).get('itemId');
  }

  clickHandler(error, itemId) {
    if (error.getIn([0, 'type']) === 'VARIABLE') {
      this.props.showVariables();
    } else if (itemId) {
      this.props.setActiveItem(itemId);
    } else if (!itemId && error.getIn([0, 'type']).startsWith('VALUESET')) {
      this.props.showValueSets();
    }
  }

  render() {
   if (this.props.errors && this.props.errors.length > 0) {
    console.log("errors", this.props.errors)
    let errorMap = this.props.errors.groupBy(e => e.get('itemId') || '$general$');
    let errors = errorMap.entrySeq().map((e, i) => {
      const uiItemId = this.resolveItemId(e[1]);
      return (
        <List.Item key={i}>
          <List.Icon name='warning sign' color={e[1].getIn([0, 'level']) != 'WARNING' ? 'red' : 'yellow'} size='large' />
          <List.Content>
            <List.Header as='a' onClick={this.clickHandler.bind(this, e[1], uiItemId)}>
              {uiItemId ? uiItemId : 'Global list'}
            </List.Header>
            {
              e[1].toSet().toList().map((m, j) => <React.Fragment key={j}>{this.translateError(m)}<br /></React.Fragment>)
            }
          </List.Content>
        </List.Item>
      );
      }
    );
   return (
    <List divided>
      {errors}
    </List>
   );
   } else {
     return null;
   }
  }
}

const ErrorListConnected = connect(
  state => ({
    errors: state.dialobComposer.editor && state.dialobComposer.editor.get('errors'),
    items: state.dialobComposer.form && state.dialobComposer.form.get('data'),
    valueSets: state.dialobComposer.form && state.dialobComposer.form.get('valueSets')
  }),
  {
    setActiveItem,
    showVariables,
    showValueSets
  }
)(ErrorList);

export {
  ErrorList,
  ErrorListConnected as default
}
