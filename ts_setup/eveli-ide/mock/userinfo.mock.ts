import { createSSEStream, defineMock } from 'vite-plugin-mock-dev-server'


export default defineMock({
  url: '/userInfo',

  /*
  response: (req, res) => {
    const sse = createSSEStream(req, res)
    sse.write({ event: 'message', data: { message: 'hello world' } })
    sse.end()
  }*/

  body: {
    authenticated: true,
    authorized: true,
    userId: 'vorst x viiner',
    email: 'super user @ super dot things',
    name: 'Carrot Top'
  }
})