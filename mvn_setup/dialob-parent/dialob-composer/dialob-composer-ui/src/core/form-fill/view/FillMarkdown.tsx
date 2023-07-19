import React from 'react';
import { Typography, Link, Table, TableHead, TableBody, TableRow, TableCell, Divider } from '@mui/material';
import ReactMarkdown from 'react-markdown';


export const FillMarkdown: React.FC<{text: string}> = ({ text }) => {
  return (
    <ReactMarkdown children={text} skipHtml components={{
      table:  ({ children }) => <Table>{children}</Table>,
      tbody:  ({ children }) => <TableBody>{children}</TableBody>,
      tr:     ({ children }) => <TableRow>{children}</TableRow>,
      th:     ({ children }) => <TableHead>{children}</TableHead>,
      td:     ({ children }) => <TableCell>{children}</TableCell>,      
      p:      ({ children }) => <Typography variant='body1' paragraph>{children}</Typography>,
      hr:     () => <Divider variant='middle' />,
      link: (props: any) => <Link href={props.href}>{props.children}</Link>,
    }} />
  );
};
