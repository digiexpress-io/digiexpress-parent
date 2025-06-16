import React from 'react';
import { Link, Breadcrumbs } from '@mui/material';



export const XfsBreadcrumbs: React.FC<{}> = () => {
  const breadcrumbs = [
    <Link
      underline="hover"
      key="2"
      color="inherit"
      href="#"
      onClick={() => {}}
    >
      Core
    </Link>
  ];

  return (
    <Breadcrumbs separator="/">
      {breadcrumbs}
    </Breadcrumbs>
  );
}