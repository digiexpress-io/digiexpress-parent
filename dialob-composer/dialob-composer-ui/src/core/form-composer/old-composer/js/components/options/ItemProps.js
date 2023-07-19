import React, {Component, useState} from 'react';
import {Table, Button, Form} from 'semantic-ui-react';
import {addItemProp, updateItemProp, deleteItemProp} from '../../actions';
import {connect} from 'react-redux';
import {InputProp} from '../propEditors';
import {findItemTypeConfig} from '../../helpers/utils';
import AutoSuggest from 'react-autosuggest';
import Immutable from 'immutable';

const PropSuggest = ({suggestions, onChange, value}) => {
  const [currentValue, setCurrentValue] = useState(value);
  const [shownSuggestions, setShownSuggestions] = useState([]);

  const handleChange = value => {
    setCurrentValue(value);
    if (onChange) {
      onChange(value);
    }
  }

  const onFetch = ({value}) => {
    setShownSuggestions(suggestions.filter(suggestion => suggestion.startsWith(value)));
  };

  const onClear = () => {
    setShownSuggestions([]);
  };

  const getSuggestionValue = suggestion => {
    return suggestion;
  };

  const renderSuggestion = suggestion => {
    return <span>{suggestion}</span>
  };

  const suggestionSelected = (e, {suggestion}) => {
    handleChange(suggestion);
  }

  const inputProps = {
    value: currentValue,
    onChange: (e) => handleChange(e.target.value)
  };

  return <AutoSuggest suggestions={shownSuggestions}
    onSuggestionsFetchRequested={onFetch}
    onSuggestionsClearRequested={onClear}
    getSuggestionValue={getSuggestionValue}
    renderSuggestion={renderSuggestion}
    onSuggestionSelected={suggestionSelected}
    alwaysRenderSuggestions={true}
    inputProps={inputProps}
    multiSection={false}
    renderSectionTitle={() => {}}
    getSectionSuggestions={() => {}}
  />;
}

class ItemProps extends Component {

  constructor(props) {
    super(props);
    this.state = {
      newItemKey: '',
      itemTypeConfig: null,
      definedProps: [],
    };
  }

  propEditor(prop, key, itemTypeConfig) {
    const itemId = this.props.item.get('id');
    const editorConfig = itemTypeConfig && itemTypeConfig.propEditors && itemTypeConfig.propEditors[prop[0]];

    const editor = (editorConfig && editorConfig.component) ?
        {
          component: editorConfig.component,
          props: editorConfig.props
        }
      : {
        component: InputProp,
        props: {}
      };

    return (
      <Table.Row key={key}>
        <Table.Cell collapsing>
          <Button size='tiny' icon='remove' onClick={() => this.props.deleteItemProp(itemId, prop[0])} />
        </Table.Cell>
        <Table.Cell collapsing>
          {prop[0]}
        </Table.Cell>
        <Table.Cell>
          <editor.component value={Immutable.Iterable.isIterable(prop[1]) ? prop[1].toJS() : prop[1]} name={prop[0]} item={this.props.item} onChange={(v) => this.props.updateItemProp(itemId, prop[0], v)} {...editor.props} />
        </Table.Cell>
      </Table.Row>);
  }

  addKey() {

    if (!this.state.newItemKey || this.state.newItemKey.trim().length === 0)  {
      return;
    }

    const key = this.state.newItemKey.trim();

    const defaultValue = this.state.itemTypeConfig &&
                         this.state.itemTypeConfig.config &&
                         this.state.itemTypeConfig.config.props ?
                         this.state.itemTypeConfig.config.props[key]
                         : null

    this.props.addItemProp(this.props.item.get('id'), key, defaultValue);
    this.setState({newItemKey: ''});
  }

  componentDidMount() {
    const {itemTypes, item} = this.props;
    const itemTypeConfig = findItemTypeConfig(itemTypes, item.get('view') || item.get('type'));
    const definedProps = itemTypeConfig && itemTypeConfig.propEditors ? Object.keys(itemTypeConfig.propEditors) : [];
    this.setState({itemTypeConfig, definedProps});
  }

  render() {
    const {item} = this.props;
    const {itemTypeConfig, definedProps, newItemKey} = this.state;
    const props = item.get('props') && item.get('props');
    const rows = props ? props.entrySeq().map((p, i) => this.propEditor(p, i, itemTypeConfig)) : [];
    const suggestions = props ? definedProps.filter(p => !props.has(p)) : definedProps;

    return (
      <React.Fragment>
      <div style={{display: 'flex', flexDirection: 'row'}}>
          <div style={{paddingRight: '10px'}}>
            <label>Add property</label>
            <PropSuggest suggestions={suggestions} onChange={value => this.setState({newItemKey: value})} value={newItemKey}/>
          </div>
          <div>
            <Form.Button width={2} fluid label={'\u00A0'} disabled={!newItemKey} onClick={() => this.addKey()}>Add</Form.Button>
          </div>
        </div>
        <Table celled compact>
          <Table.Header>
            <Table.Row>
              <Table.HeaderCell collapsing></Table.HeaderCell>
              <Table.HeaderCell>Name</Table.HeaderCell>
              <Table.HeaderCell>Value</Table.HeaderCell>
            </Table.Row>
          </Table.Header>
          <Table.Body>
            {rows}
          </Table.Body>
        </Table>
      </React.Fragment>
    );
  }
}

const ItemPropsConnected = connect(
  state => ({
    itemTypes:  state.dialobComposer.config.itemTypes
  }), {
    addItemProp,
    updateItemProp,
    deleteItemProp
  }
)(ItemProps);

export {
  ItemPropsConnected as default,
  ItemProps
};
