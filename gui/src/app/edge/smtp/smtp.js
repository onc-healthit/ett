var edgeSmtp = angular.module('ttt.edge.smtp', []);

edgeSmtp.config(['$stateProvider',
	function($stateProvider) {
		$stateProvider.state('edge.smtp.main', {
				url: '',
				views: {
					"smtp": {
						templateUrl: 'edge/smtp/smtpMain.tpl.html'
					}
				}
			})
			.state('edge.smtp.description', {
				url: '/description/:id',
				views: {
					"smtp": {
						templateUrl: 'edge/smtp/description/testDescription.tpl.html'
					}
				}
			})
			.state('edge.smtp.logs', {
				url: '/logs',
				views: {
					"smtp": {
						templateUrl: 'edge/smtp/logs/testLog.tpl.html'
					}
				}
			});

	}
]);

edgeSmtp.controller('SmtpCtrl', ['$scope', 'LogInfo', 'SMTPTestCasesDescription', 'SMTPTestCases', '$q', '$timeout', '$window', 'SMTPProfileFactory', 'SMTPLogFactory', 'growl', '$state', '$uibModal', 'ApiUrl', 'DirectRICertFactory',
	function($scope, LogInfo, SMTPTestCasesDescription, SMTPTestCases, $q, $timeout, $window, SMTPProfileFactory, SMTPLogFactory, growl, $state, $uibModal, ApiUrl, DirectRICertFactory) {

		// Certificate upload
		$scope.fileInfo = {
			"flowChunkNumber": "",
			"flowChunkSize": "",
			"flowCurrentChunkSize": "",
			"flowTotalSize": "",
			"flowIdentifier": "",
			"flowFilename": "",
			"flowRelativePath": "",
			"flowTotalChunks": ""
		};

		$scope.apiUrl = ApiUrl.get();

		$scope.successUpload = function(message) {
			$scope.fileInfo = angular.fromJson(message);
			var certFilePath = $scope.fileInfo.flowRelativePath;
			DirectRICertFactory.save(certFilePath, function(data) {
				growl.success("Certificate Uploaded", {});
			}, function(data) {
				throw {
					code: data.data.code,
					url: data.data.url,
					message: data.data.message
				};
			});
		};

		// Smtp
		$scope.senderTests = [];
		$scope.receiverTests = [];

		// Check type Edge or Hisp
		$scope.isEdge = $state.current.data.sutEge;
		// Variable for the log and description links
		if ($scope.isEdge) {
			$scope.testSystem = "edge";
		} else {
			$scope.testSystem = "hisp";
		}
		$scope.edgeProtocol = $state.current.data.protocol;

		SMTPTestCasesDescription.getTestCasesDescription(function(response) {
			var result = response.data;

			$scope.testingMode = result.testingMode;
			angular.forEach(result.tests, function(test) {
				test.status = 'na';
				test.testResult = [{
					"criteriaMet": "NA"
				}];
				// Check the protocol first
				if (test.protocol === $scope.edgeProtocol) {
					// Check the system edge or hisp
					if ((test.sutEdge && $scope.isEdge) || (test.sutHisp && !$scope.isEdge)) {
						if (test.sutRole.toLowerCase().indexOf('sender') >= 0) {
							$scope.senderTests.push(test);
						} else if (test.sutRole.toLowerCase().indexOf('receiver') >= 0) {
							$scope.receiverTests.push(test);
						}
					}
				}
			});

		});

		$scope.refreshProfile = function(current) {
			SMTPProfileFactory.query(function(data) {
				if (data.length > 0) {
					$scope.currentProfile = current || data[0];
				} else {
					$scope.currentProfile = {};
					$scope.currentProfile.profileName = "Default Profile";
				}
				$scope.profileList = data;
			});
		};

		$scope.refreshProfile();

		$scope.switchProfile = function(profile) {
			$scope.currentProfile = profile;
		};

		$scope.$watch('transactionType', function() {
			if ($scope.transactionType === 'sender') {
				$scope.testBench = $scope.senderTests;
			} else {
				$scope.testBench = $scope.receiverTests;
			}
		});

		$scope.scrollTop = function() {
			$window.scrollTo(0, 0);
		};

		$scope.displayLog = function(test) {
			$scope.logToDisplay = test;
		};

		$scope.resetTest = function(test) {
			if (!test.testResult.$resolved) {
				test.testResult.cancel();
			}
			test.status = 'na';
		};

		$scope.reset = function() {
			$scope.currentProfile.sutSMTPAddress = "";
			$scope.currentProfile.sutEmailAddress = "";
			$scope.currentProfile.sutUsername = "";
			$scope.currentProfile.sutPassword = "";
			$scope.currentProfile.profileName = "Default Profile " + $scope.profileList.length;
			$scope.refreshProfile($scope.currentProfile);
		};

		$scope.saveProfile = function(profile) {
			SMTPProfileFactory.save(profile, function() {
				growl.success("Profile saved", {});
				$scope.refreshProfile(profile);
			}, function(data) {
				throw {
					code: data.data.code,
					url: data.data.url,
					message: data.data.message
				};
			});
		};

		$scope.removeProfile = function(profile_name) {
			SMTPProfileFactory.removeProf({
				'profile': profile_name
			}, function() {
				growl.success("Profile deleted", {});
				$scope.refreshProfile();
			}, function(data) {
				throw {
					code: data.data.code,
					url: data.data.url,
					message: data.data.message
				};
			});
		};

		$scope.validateManual = function(test, validation) {
			test.status = validation;
			$scope.logTestData(test);
		};

		$scope.startTest = function(test, fieldInput) {
			// Get CCDA R2 validation objectives if exists
			var ccdaReferenceFilename = "";
			var ccdaValidationObjective = "";
			var fileLink = "";
			if (fieldInput.ccdaDocument) {
				ccdaReferenceFilename = fieldInput.ccdaDocument.name || "";
				fileLink = fieldInput.ccdaDocument.link || "";
				ccdaValidationObjective = fieldInput.ccdaDocument.path[fieldInput.ccdaDocument.path.length - 1] || "";
			}

			var previousTR = null;
            if (!angular.isUndefined(test.testResult)){
			if (test.testResult.length > 0) {
				if (test.testResult[0].criteriaMet !== "NA") {
					previousTR = test.testResult[0];
				}
			}
             }

			// Get profile info
			$scope.inputForTest = {
				"testCaseNumber": test.id,
				"sutSmtpAddress": $scope.currentProfile.sutSMTPAddress,
				"sutSmtpPort": $scope.sutSmtpPort,
				"tttSmtpPort": $scope.tttSmtpPort,
				"sutEmailAddress": $scope.currentProfile.sutEmailAddress,
				"tttEmailAddress": $scope.tttEmailAddress,
				"useTLS": true,
				"sutCommandTimeoutInSeconds": fieldInput.sutCommandTimeoutInSeconds,
				"sutUserName": $scope.currentProfile.sutUsername,
				"sutPassword": $scope.currentProfile.sutPassword,
				"tttUserName": "",
				"tttPassword": "",
				"tttSmtpAddress": $scope.tttSmtpAddress,
				"startTlsPort": 0,
				"status": test.status,
				"attachmentType": fieldInput.attachmentType,
				"ccdaReferenceFilename": ccdaReferenceFilename,
				"ccdaValidationObjective": ccdaValidationObjective,
				"ccdaFileLink": fileLink,
				"previousResult": previousTR || null
			};

			// Set status to loading for loading UI
			test.status = "loading";

			test.testResult = SMTPTestCases.startTest($scope.inputForTest, function(data) {
				// Set log result
				test.testResult = data;

				// See if the test passed, failed, pending or manual
				test.status = 'success';
				angular.forEach(data, function(res) {
					if (res.criteriaMet === 'FALSE') {
						test.status = 'fail';
					} else if (res.criteriaMet === 'MANUAL') {
						test.status = 'manual';
					} else if (res.criteriaMet === 'STEP2') {
						test.status = 'fetching';
					}
				});

				$scope.logTestData(test);

			}, function(data) {
				test.status = 'fail';
				if (data.data) {
					throw {
						code: data.data.code,
						url: data.data.url,
						message: data.data.message
					};
				}
			});
		};

		$scope.logTestData = function(test) {
			// Save log result to database
			// Get login first
			$scope.userInfo = LogInfo.getUsername();
			$scope.userInfo.$promise.then(function(logData) {
				if (logData.logged) {
					angular.forEach(test.testResult, function(smtpRes) {

						// Transform testRequestResponse
						var testResp = "";
						angular.forEach(smtpRes.testRequestResponses, function(resValue, resKey) {
							testResp += resKey;
						});

						if (test.status === 'fail' || test.status === 'success') {
							var isCriteriaMet = false;
							if (test.status === 'success') {
								isCriteriaMet = true;
							}
							var log = {
								"testCaseNumber": test.name,
								"criteriaMet": isCriteriaMet,
								"testRequestsResponse": testResp,
								"attachments": []
							};

							// Save the log to the profile
							SMTPLogFactory.save({
								'profile': $scope.currentProfile.profileName
							}, log, function(saveResult) {

							}, function(saveData) {
								test.status = 'fail';
								throw {
									code: saveData.data.code,
									url: saveData.data.url,
									message: saveData.data.message
								};
							});
						}

					});
				}
			});
		};

		$scope.getBlob = function(filename, attachment) {
			var contentType = 'text/plain';
			if (filename.indexOf('.xml') > -1) {
				contentType = 'application/xml';
			}
			return new Blob([attachment], {
				type: contentType
			});
		};

		$scope.openCcdaValidationReport = function(key, report) {
			var modalInstance = $uibModal.open({
				templateUrl: 'CCDAModalContent.html',
				controller: 'CCDAModalReportCtrl',
				size: 'lg',
				backdrop: true,
				windowClass: 'ccda-modal',
				resolve: {
					report: function() {
						return report;
					},
					key: function() {
						return key;
					}
				}
			});
		};

	}
]);

edgeSmtp.controller('CCDAModalReportCtrl', ['$scope', '$uibModalInstance', 'key', 'report',
	function($scope, $uibModalInstance, key, report) {
		$scope.key = key;
		$scope.report = report;

		$scope.ok = function() {
			$uibModalInstance.close('close');
		};

		$scope.cancel = function() {
			$uibModalInstance.dismiss('cancel');
		};
	}
]);
