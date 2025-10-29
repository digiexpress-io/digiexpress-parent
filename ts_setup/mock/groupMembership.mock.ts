import { defineMock } from 'vite-plugin-mock-dev-server'

export default defineMock({
  url: '/groupMembership',

  body: [
    { userName: "Jerry Springer", userEmail: "jerry@work.com" },
    { userName: "Sylvester Stallone", userEmail: "muscles@gym.com" },
    { userName: "Giovanni Franco", userEmail: "someGuy@franco-abc.com" },
    { userName: "Amanda Mansfred", userEmail: "amanda_mansfred@work.com" },
    { userName: "Gertrude Fibinacci", userEmail: "gertie@work.com" },
    { userName: "Howard Duck", userEmail: "avv-howard@work.com" },
    { userName: "Smithy McBride", userEmail: "mcbride@cheese.org" }
  ]
})


