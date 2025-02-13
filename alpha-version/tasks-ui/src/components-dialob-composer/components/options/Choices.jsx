import React, {Component} from 'react';
import {Button, Dropdown, Form, Divider} from 'semantic-ui-react';
import {connect} from 'react-redux';
import {isGlobalValueSet} from '../../helpers/utils';
import {createValueset, updateItem, makeValuesetGlobal, copyValuesetLocal} from '../../actions';
import ValueSetEditor from '../ValueSetEditor';

class Choices extends Component {

  render() {
    if (!this.props.item.get('valueSetId') || isGlobalValueSet(this.props.globalValueSets, this.props.item.get('valueSetId'))) {
      let options = this.props.globalValueSets ? this.props.globalValueSets.map((vs, key) => ({text: vs.get('label') || 'untitled' + key, value: vs.get('valueSetId')})).toJS()
          : [];
      return (
        <React.Fragment>
          
          <div style={{display: 'flex', flexDirection: 'row', width: '100%', alignItems: 'flex-end'}}>
            <div style={{paddingRight: '10px', flexGrow: 1}}>
              <Form.Field>
                <label>Select global list</label>
                <Dropdown fluid search selection options={options} value={this.props.item.get('valueSetId')}
                  onChange={(evt, data) => this.props.updateItem(this.props.item.get('id'), 'valueSetId', data.value)} />
              </Form.Field>
            </div>
            <div>
              <Button disabled={!this.props.item.get('valueSetId')} onClick={() => this.props.copyValuesetLocal(this.props.item.get('valueSetId'), this.props.item.get('id'))}>Copy as local</Button>
            </div>
          </div>
         
          <Divider horizontal>Or</Divider>
          <Button onClick={() => this.props.createValueset(this.props.item.get('id'))}>Create local list</Button>
        </React.Fragment>
      );
    } else  {
      return (
        <React.Fragment>
          <ValueSetEditor valueSetId={this.props.item.get('valueSetId')} />
          <Button onClick={() => this.props.makeValuesetGlobal(this.props.item.get('valueSetId'))}>Make Global</Button>
        </React.Fragment>
      );
    }
  }
}

const ChoicesConnected = connect(
  state => ({
    globalValueSets: state.dialobComposer.form && state.dialobComposer.form.getIn(['metadata', 'composer', 'globalValueSets'])
  }), {
    createValueset,
    updateItem,
    makeValuesetGlobal,
    copyValuesetLocal
  }
)(Choices);

export {
  ChoicesConnected as default,
  Choices
};
