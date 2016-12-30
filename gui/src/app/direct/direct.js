var direct = angular.module('ttt.direct', [
	// Modules
	'ttt.direct.home',
	'ttt.direct.register',
	'ttt.direct.send',
	'ttt.direct.validator',
	'ttt.direct.ccdaValidator',
	'ttt.direct.ccdar2Validator',
	'ttt.direct.status',
	'ttt.direct.report',
	'ttt.direct.help'
]);

direct.config(['$stateProvider',
	function($stateProvider) {
		$stateProvider.state('direct', {
				url: '/direct',
				params: {
					paramsObj: null
				},
				abstract: true,
				views: {
					"main": {
						controller: 'DirectCtrl',
						templateUrl: 'direct/direct.tpl.html'
					}
				},
				data: {
					pageTitle: 'Direct'
				}
			})
			.state('direct.home', {
				url: '',
				params: {
					paramsObj: null
				},
				views: {
					"direct": {
						controller: 'DirectHomeCtrl',
						templateUrl: 'direct/direct-home/direct-home.tpl.html'
					}
				}
			})
			.state('direct.register', {
				url: '/register',
				views: {
					"direct": {
						controller: 'RegisterCtrl',
						templateUrl: 'direct/register/register.tpl.html'
					}
				}
			})
			.state('direct.send', {
				url: '/send',
				views: {
					"direct": {
						controller: 'DirectSendCtrl',
						templateUrl: 'direct/send/send.tpl.html'
					}
				}
			})
			.state('direct.validator', {
				url: '/validator/direct',
				views: {
					"direct": {
						controller: 'DirectValidatorCtrl',
						templateUrl: 'direct/message-validator/direct/message-validator.tpl.html'
					}
				}
			})
			.state('direct.ccda', {
				url: '/validator/ccda',
				views: {
					"direct": {
						controller: 'CCDAValidatorCtrl',
						templateUrl: 'direct/message-validator/ccda/ccda-validator.tpl.html'
					}
				}
			})
			.state('direct.ccdar2', {
				url: '/validator/ccda/r2',
				views: {
					"direct": {
						controller: 'CCDAR2ValidatorCtrl',
						templateUrl: 'direct/message-validator/ccda-r2/ccda-r2.tpl.html'
					}
				}
			})
			.state('direct.status', {
				url: '/status',
				views: {
					"direct": {
						controller: 'DirectStatusCtrl',
						templateUrl: 'direct/status/status.tpl.html'
					}
				}
			})
			.state('direct.report', {
				url: '/report/:message_id',
				views: {
					"direct": {
						controller: 'DirectReportCtrl',
						templateUrl: 'direct/validation-report/validation-report.tpl.html'
					}
				}
			})
			.state('direct.documents', {
				url: '/documents',
				views: {
					"direct": {
						controller: 'DocumentsCtrl',
						templateUrl: 'templates/documents.tpl.html'
					}
				}
			})
			.state('direct.help', {
				url: '/help',
				views: {
					"direct": {
						controller: 'DirectHelpCtrl',
						templateUrl: 'direct/help/help.tpl.html'
					}
				}
			})
			.state('direct.changePassword', {
				url: '/changePassword',
				views: {
					"direct": {
						controller: 'ChangePasswordCtrl',
						templateUrl: 'templates/changePassword.tpl.html'
					}
				}
			})
			.state('direct.accountInfo', {
				url: '/accountInfo',
				views: {
					"direct": {
						controller: 'AccountInfoCtrl',
						templateUrl: 'templates/accountInfo.tpl.html'
					}
				}
			});
	}
]);

direct.controller('DirectCtrl', ['$scope', '$stateParams', 'SettingsFactory', 'PropertiesFactory',
	function($scope, $stateParams, SettingsFactory, PropertiesFactory) {
    $scope.paramsObj =  $stateParams.paramsObj;
console.log("DirectCtrl ....."+angular.toJson($scope.paramsObj,true));

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
