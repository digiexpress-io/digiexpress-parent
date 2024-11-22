import type React from 'react';
import { Typography, Link, Table, TableHead, TableBody, TableRow, TableCell, Divider } from '@mui/material';

import Markdown from 'react-markdown';

const HLEVEL_MAPPING: ('h1' | 'h2' | 'h3' | 'h4' | 'h5' | 'h6')[] = [
  'h1',
  'h2',
  'h3',
  'h4',
  'h5',
  'h6'
];

const renderers: Markdown.TransformOptions = {
  components: {
    header: (props: any) => <Typography variant={HLEVEL_MAPPING[props.level - 1]} gutterBottom>{props.children}</Typography>,
    p: (props: any) => <Typography variant='body1' paragraph>{props.children}</Typography>,
    link: ({ children, href }) => <Link href={href}>{children}</Link>,
    table: ({ children }) => <Table>{children}</Table>,
    th: ({ children }) => <TableHead>{children}</TableHead>,
    tbody: ({ children }) => <TableBody>{children}</TableBody>,
    tr: ({ children }) => <TableRow>{children}</TableRow>,
    td: ({ children }) => <TableCell>{children}</TableCell>,
    hr: () => <Divider variant='middle' />,
    li: ({ children }) => <Typography variant='body1'>{children}</Typography>,
  }
};

export interface MarkdownViewProps {
  text: string;
}

export const GFormReviewMarkdownView: React.FC<MarkdownViewProps> = ({ text }) => {
  return (
    <Markdown children={text} skipHtml components={renderers.components} />
  );
}
