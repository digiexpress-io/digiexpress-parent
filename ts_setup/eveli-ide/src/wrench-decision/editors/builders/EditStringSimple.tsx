import React from 'react'
import MDEditor, { ICommand, commands } from '@uiw/react-md-editor';
import { TextareaAutosize } from '@mui/material';
import { StringBuilder } from './'
import { HdesApi } from '@/api-wrench';


const MdLocaleSelect: React.FC<{ locale: string }> = ({ locale }) => {
  return (
    <div style={{ fontWeight: 'bold', fontSize: 15, alignItems: 'center' }}>
      {locale}
    </div>);
}

const getMdCommands = (
  dt: HdesApi.AstDecision,
  header: HdesApi.TypeDef,
) => {
  const localeTitle: ICommand = {
    name: header.name,
    groupName: 'title',
    keyCommand: 'title1',
    buttonProps: { },
    icon: (<MdLocaleSelect locale={header.name} />)
  };

  return [
    localeTitle,
    commands.group([commands.title1, commands.title2, commands.title3, commands.title4, commands.title5, commands.title6], {
      name: 'title',
      groupName: 'title',
      buttonProps: { 'aria-label': 'Insert title' },

    }),
    commands.bold,
    commands.italic,
    commands.strikethrough,
    commands.hr,
    commands.divider,
    commands.link,
    commands.quote,
    commands.code,
    commands.codeBlock,
    commands.image,
    commands.divider,
    commands.unorderedListCommand,
    commands.orderedListCommand,
    commands.checkedListCommand,
  ];
}

export const EditStringSimple: React.FC<{
  dt: HdesApi.AstDecision,
  header: HdesApi.TypeDef,
  builder: StringBuilder, 
  onChange: (value: string) => void
}> = ({ builder, onChange, dt, header }) => {

  if(dt.name.endsWith('locale') || dt.name.endsWith('intl')) {
    return (
      <MDEditor 
        value={builder.value} 
        onChange={(value) => onChange(value ?? '')} 
        commands={getMdCommands(dt, header)}
        textareaProps={{ placeholder: '# Title' }}
        height={800}
      />);
  }

  return (<TextareaAutosize 
    minRows={10}
    style={{ width: '100%', height: '100%' }}
    value={builder.value}
    onChange={({ target }) => onChange(target.value)}
  />);
}