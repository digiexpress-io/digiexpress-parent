import React, {Component} from 'react';
import {Dropdown, Segment, Form} from 'semantic-ui-react';
import {connect} from 'react-redux';
import {updateItem} from '../../actions'
import Immutable from 'immutable';

class Styleclasses extends Component {

  onChange(v) {
    this.props.setAttribute('className', Immutable.List(v.value));
  }

  render() {
    const className = this.props.item.get('className');
    const value = className ? className.toJS() : [];
    const options = className ? className.map(c => ({key: c, text: c, value: c})).toJS() : [];
    // TODO: Add preset classnames from item config
    return (
      <Form>
        <Form.Field>
        <label>Select active style classes</label>
        <Dropdown placeholder='Style classes' allowAdditions fluid multiple search selection
         options={options} value={value} onChange={(evt, data) => this.onChange(data)} />
        </Form.Field>
      </Form>
    );
  }
}

const StyleclassesConnected = connect(
  state => ({}),
  (dispatch, props) => ({
    setAttribute: (attribute, value, language = null) => dispatch(updateItem(props.item.get('id'), attribute, value, language)),
  })
)(Styleclasses);

export {
  StyleclassesConnected as default,
  Styleclasses
};
