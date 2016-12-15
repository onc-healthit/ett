var certMod = angular.module('ttt.certification', [
    // Modules
    'ttt.certification.certh1'
]);

certMod.config(['$stateProvider',
    function($stateProvider) {
        $stateProvider.state('certification', {
            url: '/certification',
            abstract: true,
            views: {
                "main": {
                    controller: 'CertificationCriteriaCtrl',
                    templateUrl: 'certification/certificationCriteria.tpl.html'
                }
            },
            data: {
                pageTitle: 'Certification'
            }
        })
            .state('certification.home', {
                url: '',
                views: {
                    "certification": {
                        controller: 'EdgeHomeCtrl',
                        templateUrl: 'edge/edge-home/edge-home.tpl.html'
                    }
                }
            })
            .state('certification.certb1', {
                url: '/certb1',
                views: {
                    "certification": {
                        controller: 'CertificationCriteriaCtrl',
                        templateUrl: 'certification/certificationCriteriab1.tpl.html'
                    }
                }
            })
            .state('certification.certh1', {
                url: '/certh1',
                abstract: true,
                views: {
                    "certification": {
                        controller: 'Certh1Ctrl',
                        //controller: 'SmtpCtrl',
                        //templateUrl: 'edge/smtp/smtp.tpl.html'
                        templateUrl: 'certification/certificationCriteriah1.tpl.html'
                    }
                },
                data: {
                    sutEge: true,
                    protocol: "smtp"
                }
            })
            .state('certification.certh2', {
                url: '/certh2',
                views: {
                    "certification": {
                        controller: 'CertificationCriteriaCtrl',
                        templateUrl: 'certification/certificationCriteriah2.tpl.html'
                    }
                }
            })
            .state('certification.documents', {
                url: '/documents',
                views: {
                    "certification": {
                        controller: 'DocumentsCtrl',
                        templateUrl: 'templates/documents.tpl.html'
                    }
                }
            })
            .state('certification.help', {
                url: '/help',
                views: {
                    "certification": {
                        controller: 'EdgeHelpCtrl',
                        templateUrl: 'edge/help/help.tpl.html'
                    }
                }
            });

    }
]);

certMod.controller('CertificationCriteriaCtrl', ['$scope', 'PropertiesFactory',
    function($scope, PropertiesFactory) {
        PropertiesFactory.get(function(result) {
            $scope.properties = result;
        });
    }
]);