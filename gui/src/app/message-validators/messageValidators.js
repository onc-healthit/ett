var validators = angular.module('ttt.validators', [
    // Modules
    'ttt.direct.ccdar2Validator',
    'ttt.direct.ccdaValidator',
    'ttt.direct.xdmValidator'
]);

validators.config(['$stateProvider',
    function($stateProvider) {
        $stateProvider.state('validators', {
                url: '/validators',
                abstract: true,
                views: {
                    "main": {
                        controller: 'MessageValidatorsCtrl',
                        templateUrl: 'message-validators/messageValidators.tpl.html'
                    }
                },
                data: {
                    pageTitle: 'Message Validators'
                }
            })
            .state('validators.ccdar2', {
                url: '',
                views: {
                    "validators": {
                        controller: 'CCDAR2ValidatorCtrl',
                        templateUrl: 'direct/message-validator/ccda-r2/ccda-r2.tpl.html'
                    }
                }
            })
            .state('validators.ccdar1', {
                url: '/ccdar1',
                views: {
                    "validators": {
                        controller: 'CCDAValidatorCtrl',
                        templateUrl: 'direct/message-validator/ccda/ccda-validator.tpl.html'
                    }
                }
            }).state('validators.xdm', {
                url: '/xdm',
                views: {
                    "validators": {
                        controller: 'XDMValidatorCtrl',
                        templateUrl: 'message-validators/xdm-validator/xdm-validator.tpl.html'
                    }
                }
            }).state('validators.xdr', {
                url: '/xdr',
                views: {
                    "validators": {
                        controller: 'XDRValidatorCtrl',
                        templateUrl: 'message-validators/xdr-validator/xdr-validator.tpl.html'
                    }
                }
            }).state('validators.direct', {
                url: '/direct',
                views: {
                    "validators": {
                        controller: 'DirectValidatorCtrl',
                        templateUrl: 'direct/message-validator/direct/message-validator.tpl.html'
                    }
                }
            }).state('validators.documents', {
                url: '/documents',
                views: {
                    "validators": {
                        controller: 'DocumentsCtrl',
                        templateUrl: 'templates/documents.tpl.html'
                    }
                }
            })
            .state('validators.help', {
                url: '/help',
                views: {
                    "validators": {
                        controller: 'EdgeHelpCtrl',
                        templateUrl: 'edge/help/help.tpl.html'
                    }
                }
            })
            .state('validators.changePassword', {
                url: '/changePassword',
                views: {
                    "validators": {
                        controller: 'ChangePasswordCtrl',
                        templateUrl: 'templates/changePassword.tpl.html'
                    }
                }
            })
            .state('validators.accountInfo', {
                url: '/accountInfo',
                views: {
                    "validators": {
                        controller: 'AccountInfoCtrl',
                        templateUrl: 'templates/accountInfo.tpl.html'
                    }
                }
            }).state('validators.releaseNotes', {
                url: '/releaseNotes',
                views: {
                    "validators": {
                        controller: 'ReleaseNotesCtrl',
                        templateUrl: 'templates/releaseNotes.tpl.html'
                    }
                }
            });
    }
]);

validators.controller('MessageValidatorsCtrl', ['$scope', 'SettingsFactory', 'PropertiesFactory',
    function($scope, SettingsFactory, PropertiesFactory) {
        
        SettingsFactory.getSettings(function(result) {
            $scope.settings = result.data;
        }, function(error) {
            $scope.error = true;
        });

        PropertiesFactory.get(function(result) {
            $scope.properties = result;
        });

    }
]);
