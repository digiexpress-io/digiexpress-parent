const webpack = require('webpack');

module.exports = function override(config) {
  const fallback = config.resolve.fallback || {};
  Object.assign(fallback, {
    /*
    url: require.resolve("url"),
    assert: require.resolve("assert"),
    crypto: require.resolve("crypto-browserify"),
    http: require.resolve("stream-http"),
    https: require.resolve("https-browserify"),
    os: require.resolve("os-browserify/browser"),
    
    buffer: require.resolve("buffer"),
    stream: require.resolve("stream-browserify"),*/
    path: require.resolve("path-browserify"),
    process: require.resolve("process"),
  })
  config.resolve.fallback = fallback;
  config.plugins = (config.plugins || []).concat([
    new webpack.ProvidePlugin({
      process: 'process/browser',
      //Buffer: ['buffer', 'Buffer']
    })
  ]);

  config.stats = { ...config.stats, children: true };

  // react-dnd
  config.module.rules.unshift({
    test: /\.m?js$/,
    resolve: {
      fullySpecified: false, // disable the behaviour
    },
  });

  // react-dnd
  config.resolve.alias = {
    ...config.resolve.alias,
    "react/jsx-runtime.js": "react/jsx-runtime",
    "react/jsx-dev-runtime.js": "react/jsx-dev-runtime",
  };
  
  config.ignoreWarnings = [/Failed to parse source map/];
  return config;
}