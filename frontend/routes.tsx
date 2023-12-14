import HelloWorldView from 'Frontend/views/HelloWorldView.js';
import MainLayout from 'Frontend/views/MainLayout.js';
import { lazy } from 'react';
import { createBrowserRouter, RouteObject } from 'react-router-dom';
import Cursors from './views/Cursors';
import TodoMVC from './views/TodoMVC';
import CounterView from './views/CounterView';

export const routes = [
  {
    element: <MainLayout />,
    handle: { title: 'Main' },
    children: [
      { path: '/', element: <HelloWorldView />, handle: { title: 'Hello World' } },
      { path: '/cursors', element: <Cursors />, handle: { title: 'Cursors' } },
      { path: '/todos', element: <TodoMVC />, handle: { title: 'TodoMVC' } },
      { path: '/counter', element: <CounterView />, handle: { title: 'Counter' } },
    ],
  },
] as RouteObject[];

export default createBrowserRouter(routes);
