
// karma start --coverage [coverageType]
var coverageType = 'html';
var coverage = process.argv.filter(function(a, index) { 
  if (a == '--coverage') {
    if ((index + 1) < process.argv.length) {
      coverageType = process.argv[index+1];
    }
    return true;
  }
  return false;
}).length;


// base path, that will be used to resolve files and exclude
basePath = '';

// frameworks to use
frameworks = ['mocha', 'requirejs'];
preprocessors = { };

// list of files / patterns to load in the browser
files = [
  // Source 
  {pattern: 'js/**/*.js', included: false},

  // Included libs
  'libs/jquery/jquery.js',
  'libs/jquery-ui/jquery-ui-1.10.3.custom.js',
  'libs/bootstrap/js/bootstrap.js',

  // Libraries
  {pattern: 'libs/**/*.js', included: false},

  // Test Files
  {pattern: 'test/spec/**/*.js', included: false},

  // Test runner
  'test/runner/main.js'
];


// list of files to exclude
exclude = [
  
];


// test results reporter to use
// possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
reporters = ['progress'];


if (coverage) {
  preprocessors['src/!(vendor)/**.js'] = 'coverage';

  // The above doesn't include the top level src items
  preprocessors['src/*.js'] = 'coverage';

  reporters.push('coverage');

  coverageReporter = {
    type: coverageType,
    dir: 'build/coverage/'
  };
}container-fluid


// web server port
port = 9876;


// cli runner port
runnerPort = 9100;


// enable / disable colors in the output (reporters and logs)
colors = true;


// level of logging
// possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
logLevel = LOG_INFO;


// enable / disable watching file and executing tests whenever any file changes
autoWatch = true;


// Start these browsers, currently available:
// - Chrome
// - ChromeCanary
// - Firefox
// - Opera
// - Safari (only Mac)
// - PhantomJS
// - IE (only Windows)
//browsers = ['Chrome', 'Safari', 'Firefox', 'PhantomJS' ];
browsers = ['Chrome'];


// If browser does not capture in given timeout [ms], kill it
captureTimeout = 60000;


// Continuous Integration mode
// if true, it capture browsers, run tests and exit
singleRun = false;


// plugins to load
plugins = [
  'karma-mocha',
  'karma-requirejs',
  'karma-coverage',
  'karma-chrome-launcher',
  'karma-safari-launcher',
  'karma-firefox-launcher',
  'karma-phantomjs-launcher'
];

