import React from 'react';
const scroll = require("react-redux-scroll");

const scrollableEditor = (EditorColumn: React.FC<{}>) => scroll.scrollableArea(EditorColumn);
const createScrollMiddleware = scroll.createScrollMiddleware;
const scrollToWhen = scroll.scrollToWhen;

export {scrollableEditor, createScrollMiddleware, scrollToWhen}