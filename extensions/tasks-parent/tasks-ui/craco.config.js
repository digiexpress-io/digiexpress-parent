const path = require('path');
module.exports = {
  webpack: {
    alias: {
      '@taskclient': path.resolve(__dirname, 'src/taskclient/index.ts'),
      '@styles': path.resolve(__dirname, 'src/styles/index.ts'),
      
    },
  },
};