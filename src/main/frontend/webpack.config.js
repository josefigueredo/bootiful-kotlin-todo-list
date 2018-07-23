var path = require('path');
var webpack = require("webpack")
module.exports = {
    entry: {
        app: './app/index.js'
    },
    resolve:{
        extensions: [".js", ".jsx"]
    },
    plugins: [
        new webpack.LoaderOptionsPlugin({
            options: {
                uglify: false
            }
        }),
        new webpack.DefinePlugin({
            'process.env': {
                NODE_ENV: JSON.stringify('development')
            }
        })
    ],
    module: {
        rules: [
            {
                test: /\.css$/,
                use: ['style-loader','css-loader']
            },
            {
                test: path.join(__dirname, "."),
                exclude: path.resolve(__dirname, "node_modules"),
                use: {
                    loader: "babel-loader",
                    options: {
                        presets: ["env", "react"]
                    }
                }

            }
        ]
    },
    output: {
        filename: 'bundle.js',
        path: path.resolve(__dirname, './dist/META-INF/resources/webjars/family-budget-ui')
    }
};