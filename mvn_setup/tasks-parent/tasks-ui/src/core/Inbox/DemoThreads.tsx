import { Thread } from "./thread-types";

export const demoThreads: Thread[] = [
  {
    id: 'THREAD_1',
    userName: 'John Doe',
    topicName: 'Education',
    formName: 'Enrollment Form',
    messages: [
      {
        id: 'MSG_11',
        userName: 'Office Worker',
        text: 'This is my message.',
        date: new Date(),
        attachments: [],
        read: true,
      },
      {
        id: 'MSG_12',
        userName: 'John Doe',
        text: 'This is a user message. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod, nisl eget aliquam ultricies, nunc nisl aliquet nunc, quis aliquam nisl nunc quis nisl. Donec euismod, nisl eget aliquam ultricies, nunc nisl aliquet nunc, quis aliquam nisl nunc quis nisl. Donec euismod, nisl eget aliquam ultricies, nunc nisl aliquet nunc, quis aliquam nisl nunc quis nisl. Donec euismod, nisl eget aliquam ultricies, nunc nisl aliquet nunc, quis aliquam nisl nunc quis nisl. Donec euismod, nisl eget aliquam ultricies, nunc nisl aliquet nunc, quis aliquam nisl nunc quis nisl. Donec euismod, nisl eget aliquam ultricies, nunc nisl aliquet nunc, quis aliquam nisl nunc quis nisl. Donec euismod, nisl eget aliquam ultricies, nunc nisl aliquet nunc, quis aliquam nisl nunc quis nisl.',
        date: new Date(),
        attachments: [
          {
            id: 'ATT_11',
            name: 'image.png',
          }
        ],
        read: true,
        replyToId: 'MSG_11',
      },
      {
        id: 'MSG_13',
        userName: 'John Doe',
        text: 'This is a representer message.',
        date: new Date(),
        attachments: [],
        read: true,
        replyToId: 'MSG_12',
        representerName: 'Jane Doe',
      }
    ]
  },
  {
    id: 'THREAD_2',
    userName: 'Jane Doe',
    topicName: 'Housing',
    formName: 'Housing Form',
    messages: [
      {
        id: 'MSG_21',
        userName: 'Office Worker',
        text: 'This is my message.',
        date: new Date(),
        attachments: [],
        read: true,
      },
      {
        id: 'MSG_22',
        userName: 'Jane Doe',
        text: 'This is a user message.',
        date: new Date(),
        attachments: [],
        read: false,
        replyToId: 'MSG_21',
      },
    ]
  }
]
