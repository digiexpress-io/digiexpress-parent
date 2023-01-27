const path = require('path');
module.exports = {
  webpack: {
    alias: {
      '@declient': path.resolve(__dirname, 'src/declient/index.ts'),
      '@hdes-types': path.resolve(__dirname, 'src/declient/hdes-ast-types.ts'),
      '@styles': path.resolve(__dirname, 'src/styles/index.ts'),
    },
  },
};