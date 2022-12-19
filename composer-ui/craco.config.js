const path = require('path');
module.exports = {
  webpack: {
    alias: {
      '@declient': path.resolve(__dirname, 'src/declient/index.ts'),
    },
  },
};