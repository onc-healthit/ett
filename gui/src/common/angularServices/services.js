/* Services */


var tttService = angular.module('ttt.services', ['ngResource']);

// Constants for services
tttService.constant('RESOURCES', (function() {
    // Define your variable
    var resource = '';
    // Use the variable in your constants
    return {
        USERS_DOMAIN: resource,
        USERS_API: resource + 'api/'
    };
})());

tttService.service('ApiUrl', ['RESOURCES', function(RESOURCES) {
    return {
        get: function() {
            return RESOURCES.USERS_API;
        }
    };
}]);


/**
 *  ASSETS
 */

tttService.factory('SettingsFactory', function($http) {
    return {
        getSettings: function(callback, error) {
            $http.get('assets/directSettings.json').then(callback, error);
        }
    };
});

tttService.factory('ReleaseNotesFactory', function($http) {
    return {
        get: function(callback, error) {
            $http.get('assets/release_notes.txt').then(callback, error);
        }
    };
});

tttService.factory('SMTPTestCasesDescription', function($http) {
    return {
        getTestCasesDescription: function(callback, error) {
            $http.get('assets/smtptestCases.json').then(callback, error);
        }
    };
});

tttService.factory('XDRTestCasesDescription', function($http) {
    return {
        getTestCasesDescription: function(callback, error) {
            $http.get('assets/xdr_spreadsheet.json').then(callback, error);
        }
    };
});

tttService.factory('XDRTestCasesTemplate', function($http) {
    return {
        getTestCasesDescription: function(callback, error) {
            $http.get('assets/xdrtestCases.json').then(callback, error);
        }
    };
});

tttService.factory('CCDAR21Documents', function($http) {
    return {
        getCcdaDocuments: function(callback, error) {
            $http.get('assets/ccdar2list.json').then(callback, error);
        }
    };
});

tttService.factory('PropertiesFactory', ['$resource', 'RESOURCES',
    function($resource, RESOURCES) {
        return $resource(RESOURCES.USERS_API + 'properties', {}, {});
    }
]);

tttService.factory('AllLogFilesFactory', ['$resource', 'RESOURCES',
    function($resource, RESOURCES) {
        return $resource(RESOURCES.USERS_API + 'logview', {}, {});
    }
]);

tttService.factory('LogViewFactory', ['$resource', 'RESOURCES',
    function($resource, RESOURCES) {
        return $resource(RESOURCES.USERS_API + 'logview/:file', {
            file: '@file'
        }, {});
    }
]);

tttService.factory('LogViewLevelFactory', ['$resource', 'RESOURCES',
    function($resource, RESOURCES) {
        return $resource(RESOURCES.USERS_API + 'logview/level', {}, {});
    }
]);

/**
 *   LOGIN SERVICES
 */

tttService.factory('CreateUser', ['$resource', 'RESOURCES',
    function($resource, RESOURCES) {
        return $resource(RESOURCES.USERS_API + 'login/register', {}, {
            createUser: {
                method: 'POST'
            }
        });
    }
]);

tttService.factory('LogInfo', ['$resource', 'RESOURCES',
    function($resource, RESOURCES) {

        return $resource(RESOURCES.USERS_API + 'login', {}, {
            'getUsername': {
                method: 'GET',
                isArray: false
            }
        });
    }
]);

tttService.service('Login', ['$http', 'RESOURCES',
    function($http, RESOURCES) {

        this.login = function(credentials) {
            var data = "username=" + credentials.username + "&password=" + credentials.password + "&submit=Login";

            return $http.post(RESOURCES.USERS_DOMAIN + 'login', data, {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            });
        };
    }
]);

tttService.service('Logout', ['$http', 'RESOURCES',
    function($http, RESOURCES) {

        this.logout = function() {
            return $http.get(RESOURCES.USERS_DOMAIN + 'logout');
        };
    }
]);

/**
 *   PASSWORD SERVICES
 */
tttService.factory('ChangePassword', ['$resource', 'RESOURCES',
    function($resource, RESOURCES) {

        return $resource(RESOURCES.USERS_API + 'passwordManager/change', {}, {});
    }
]);

tttService.factory('ForgotPassword', ['$resource', 'RESOURCES',
    function($resource, RESOURCES) {

        return $resource(RESOURCES.USERS_API + 'passwordManager/forgot', {}, {});
    }
]);


/**
 *   CCDA SERVICES
 */

tttService.factory('CCDADocumentsFactory', ['$resource', 'RESOURCES',
    function($resource, RESOURCES) {
        return $resource(RESOURCES.USERS_API + 'ccdadocuments', {}, {});
    }
]);


/**
 *  DIRECT RI Cert Upload
 */
tttService.factory('DirectRICertFactory', ['$resource', 'RESOURCES',
    function($resource, RESOURCES) {
        return $resource(RESOURCES.USERS_API + 'directricert', {}, {});
    }
]);