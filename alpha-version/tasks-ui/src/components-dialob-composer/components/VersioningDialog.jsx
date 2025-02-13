import React, { Component } from "react";
import { Modal, Button, List, Header, Loader } from "semantic-ui-react";
import { connect } from "react-redux";
import { hideVersioning, fetchVersions, loadForm, downloadForm } from "../actions";
import moment from 'moment';

const VersionItem = ({name, description, created, icon, active, onActivate, onDownload}) => {
  return (
    <List.Item active={active}>
      <List.Content floated='right'>
        <Button size='tiny' disabled={active} onClick={onActivate}>Activate</Button>
        <Button size='tiny' onClick={() => onDownload(name)} icon='download' />
      </List.Content>
      <List.Icon name={icon} size='large' verticalAlign='middle' color={active ? 'blue' : null} />
      <List.Content>
    <List.Header>{name} { created && <div style={{float:'right', fontWeight: 'normal'}}><i>{moment(created).format('LLLL')}</i></div>}</List.Header>
        { description && 
        <List.Description>
          {
            description 
          }
        </List.Description>
}
      </List.Content>
    </List.Item>
  );
}

class VersioningDialog extends Component {

  componentWillUpdate(nextProps) {
    if (!this.props.versioningOpen && nextProps.versioningOpen && this.props.fetchVersions) {
      this.props.fetchVersions();
    }
  }

  renderVersions() {
   if (!this.props.versions) {
      return <Loader inline='centered' active/>;
   }

   let items = this.props.versions.map((v, i) => 
    <VersionItem key={i} name={v.get('name')} created={v.get('created')} description={v.get('description')}
       icon='tag' active={this.props.formTag === v.get('name')} onActivate={() => this.props.loadForm(this.props.formName, v.get('name') )} 
       onDownload={(tag) => this.props.downloadForm(tag)}
    />);
   items = items.push(<VersionItem key='latest' name='LATEST' created={null} icon='edit' active={!this.props.formTag} onActivate={() => this.props.loadForm(this.props.formName)} onDownload={() => this.props.downloadForm()} />);

   return (
    <List celled selection>
      {items.reverse()}
    </List>
   );
  }

  render() {
    if (this.props.versioningOpen) {
      return (
        <Modal open size='small'>
          <Modal.Header>Versioning</Modal.Header>
          <Modal.Content scrolling>
          <Header as='h4'>List of tagged versions</Header>
          <p>Only the <strong>LATEST</strong> version is editable!</p>
            {this.renderVersions()}
          </Modal.Content>
          <Modal.Actions>
            <Button primary onClick={() => this.props.hideVersioning()}>
              OK
            </Button>
          </Modal.Actions>
        </Modal>
      );
    } else {
      return null;
    }
  }
}

const VersioningDialogConnected = connect(
  state => ({
    versioningOpen:
      state.dialobComposer.editor &&
      state.dialobComposer.editor.get("versioningDialog"),
    versions:
      state.dialobComposer.editor &&
      state.dialobComposer.editor.get("versions"),
    formTag: state.dialobComposer.form.get('_tag'),
    formName: state.dialobComposer.form.get('name'),
  }),
  {
    hideVersioning,
    fetchVersions,
    loadForm,
    downloadForm
  }
)(VersioningDialog);

export { VersioningDialogConnected as default, VersioningDialog };
