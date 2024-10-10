/*-
 * #%L
 * wrench-assets-ide
 * %%
 * Copyright (C) 2016 - 2019 Copyright 2016 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import React from 'react'
import PropTypes from 'prop-types'
import { connect } from 'react-redux'

import { Modal, Header, Form, Button, Icon, Input, Dropdown } from 'semantic-ui-react'
import { FormattedMessage } from 'react-intl'

import { setHeaderEdit, setHeaderEditType, setHeaderEditChange, getCommand } from '../actions'


const directions = [
  {key: 'IN', value: 'IN', text: 'IN'},
  {key: 'OUT', value: 'OUT', text: 'OUT'}
]

class HeaderEditInternal extends React.Component {

  static get propTypes() {
    return {
      config: PropTypes.shape({ 
        id: PropTypes.string.isRequired
      }).isRequired
    }
  }

  constructor(props, context) {
    super(props, context)
    this.internalRenderer = this.internalRenderer.bind(this)
    this.externalRenderer = this.externalRenderer.bind(this)
  }

  externalRenderer() {
    const header = this.props.header

    const headerTypes = Object.values(this.props.model.headerTypes)
      .filter(h => !h.ref)
      .map(h => {
        
        const headerValue = h.value ? h.value : h;
        
        return {key: headerValue, value: headerValue, text: headerValue};
      })
    const refs = Object.values(this.props.model.headerTypes)
      .filter(h => h.ref)
      .map(h => ({key: h.ref, value: h.ref, text: h.ref}));
    refs.push({key: '', value: '', text: ''})

    const getRefHeader = (key) => {
      const result = Object.values(this.props.model.headerTypes)
        .filter(h => h.ref)
        .filter(h => h.ref === key)
      return result.length > 0 ? result[0] : null
    }
    
    return <Form>
      <Form.Field>
        <label><FormattedMessage id='dt.header.ref'/></label>
        <Dropdown scrolling search selection options={refs} defaultValue={header.get('ref')} 
          onChange={(event, {value}) => this.props.setRefChange(getRefHeader(value))}/>
      </Form.Field>
      <Form.Field>
        <label><FormattedMessage id='dt.header.name'/></label>
        <Input fluid disabled value={header.get('name')} />
      </Form.Field>
      <Form.Field>
        <label><FormattedMessage id='dt.header.dataType'/></label>
        <Dropdown key='extDataType' fluid disabled selection options={headerTypes} defaultValue={header.get('value')} />
      </Form.Field>
      <Form.Field>
        <label><FormattedMessage id='dt.header.direction'/></label>
        <Dropdown key='extDirection' disabled selection fluid options={directions} defaultValue={header.get('direction')} />
      </Form.Field>
      <Button.Group attached>
        <Button onClick={this.props.setHeaderEditType} color='blue' basic><Icon name='cogs' />
          <FormattedMessage id='dt.header.internal'/>
        </Button>
        <Button onClick={() => this.props.setDeleteHeader(header.get('id'))} color='red' basic>
          <FormattedMessage id='dt.header.delete'/>&nbsp;&nbsp;<Icon name='trash alternate outline' />
        </Button>
      </Button.Group>
    </Form>
  }
  
  internalRenderer() {
    const header = this.props.header
    const headerTypes = Object.values(this.props.model.headerTypes)
      .filter(h => !h.ref)
      .map(h => {
        const headerValue = h.value ? h.value : h;
        return {key: headerValue, value: headerValue, text: headerValue};
      })
    
    const headerExpressions = [{key: '', value: '', text: ''}]
    if(this.props.model.headerExpressions) {
      const valueType = header.get('value')
      const available = this.props.model.headerExpressions[valueType]
      if(available) {
        available.forEach(v => headerExpressions.push({key: v, value: v, text: v}))        
      }
    }

    return <Form>
      <Form.Field>
        <label><FormattedMessage id='dt.header.name'/></label>
        <Input fluid defaultValue={header.get('name')} onChange={this.props.setNameChange}/>
      </Form.Field>
      <Form.Field>
        <label><FormattedMessage id='dt.header.dataType'/></label>
        <Dropdown selection fluid options={headerTypes} defaultValue={header.get('value')} onChange={this.props.setDataTypeChange}/>
      </Form.Field>
      <Form.Field>
        <label><FormattedMessage id='dt.header.direction'/></label>
        <Dropdown selection fluid options={directions} defaultValue={header.get('direction')} onChange={this.props.setDirectionChange}/>
      </Form.Field>
      {this.props.header.get('direction') === 'OUT' ? null :
      <Form.Field>
        <label><FormattedMessage id='dt.header.expression'/>{}</label>
        <Dropdown selection fluid options={headerExpressions} onChange={this.props.setExpressionChange}/>
      </Form.Field>}
      
      {this.props.header.get('direction') === 'IN' ? null :
        <Form.Field>
          <label><FormattedMessage id='dt.header.script'/></label>
          <Input fluid defaultValue={header.get('script')} onChange={this.props.setScriptChange}/>
        </Form.Field>}
          
      <Button.Group attached>
        <Button onClick={this.props.setHeaderEditType} disabled={header.get('direction') === 'OUT'} color='blue' basic>
          <Icon name='cogs' /><FormattedMessage id='dt.header.external'/>
        </Button>
        <Button onClick={() => this.props.setDeleteHeader(header.get('id'))} color='red' basic>
          <FormattedMessage id='dt.header.delete'/>&nbsp;&nbsp;<Icon name='trash alternate outline' />
        </Button>
      </Button.Group>
    </Form>
  }
  
  render() {
    const value = this.props.header
    if(!value) {
      return null
    }

    const header = {name: this.props.model.name, column: value.get('name')}
    return <Modal open={true}  onClose={() => this.props.setCommands(value)}>
        <Header icon='edit' content={<FormattedMessage id='dt.header.edit' values={header}/>} />
        <Modal.Content>{value.get('external') ? this.externalRenderer() : this.internalRenderer()}</Modal.Content>
      </Modal>
  }
}
  
export const HeaderEdit = connect(
  (state, props) => {
    return {
      model: state.dt.getIn(['models', props.config.id]),
      header: state.dt.get('header'),
    }
  },
  (dispatch, props) => {
    return {
      setCommands: (header) => {
        const id = header.get('id')
        let change = header.get('change')
        if(change.get('ref') && !header.get('external')) {
          change = change.set('ref', null)
        }
        const commands = change.keySeq().toArray().map(key => {
          switch(key) {
          case 'name': return {type: 'SET_HEADER_REF', id: id, value: change.get('name')}
          case 'ref': return {type: 'SET_HEADER_EXTERNAL_REF', id: id, value: change.get('ref')}
          case 'value': return {type: 'SET_HEADER_TYPE', id: id, value: change.get('value')}
          case 'script': return {type: 'SET_HEADER_SCRIPT', id: id, value: change.get('script')}
          case 'direction': return {type: 'SET_HEADER_DIRECTION', id: id, value: change.get('direction')}
          case 'expression': return {type: 'SET_HEADER_EXPRESSION', id: id, value: change.get('expression')} }
        })
        dispatch(getCommand(props.config.id, commands, setHeaderEdit()))
      },
      setRefChange: (refType) => {
        if(refType) {
          dispatch(setHeaderEditChange('ref', refType.ref))
          dispatch(setHeaderEditChange('name', refType.name))
          dispatch(setHeaderEditChange('value', refType.value))
        } else {
          dispatch(setHeaderEditChange('ref', null))
        }
      },
      setDeleteHeader: (id) => dispatch(getCommand(props.config.id, {type: 'DELETE_HEADER', id: id}, setHeaderEdit())),
      setNameChange: (event, {value}) => dispatch(setHeaderEditChange('name', value)),
      setDataTypeChange: (event, {value}) => dispatch(setHeaderEditChange('value', value)),
      setDirectionChange: (event, {value}) => dispatch(setHeaderEditChange('direction', value)),
      setExpressionChange: (event, {value}) => dispatch(setHeaderEditChange('expression', value)),
      setScriptChange: (event, {value}) => dispatch(setHeaderEditChange('script', value)),
      setHeaderEditType: () => dispatch(setHeaderEditType())
    }
}) (HeaderEditInternal)
