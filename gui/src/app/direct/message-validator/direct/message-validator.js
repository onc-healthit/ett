var directValidator = angular.module('ttt.direct.validator', []);

directValidator.controller('DirectValidatorCtrl', ['$scope', 'MessageValidatorFactory', 'PropertiesFactory','growl','CCDADocumentsFactory','$state', 'ApiUrl',
    function($scope, MessageValidatorFactory, PropertiesFactory, growl, CCDADocumentsFactory, $state, ApiUrl) {

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

        $scope.validator = {
            "messageFilePath": "",
            "certFilePath": "",
            "certPassword": ""
        };

		$scope.sutRole = "sender";
		$scope.ccdaData = {};

		CCDADocumentsFactory.get(function(data) {
			$scope.ccdaDocuments = data;
			if (data !== null) {
				$scope.sutRole = Object.keys(data)[0];
				$scope.ccdaData = $scope.ccdaDocuments[$scope.sutRole];
			}
		}, function(error) {
			console.log(error);
		});

		$scope.switchDocType = function(type) {
			$scope.sutRole = type;
			$scope.ccdaData = $scope.ccdaDocuments[$scope.sutRole];
		};

		$scope.displayGrowl = function(text) {
			growl.success(text);
		};

        $scope.successMessage = function(message) {
            $scope.fileInfo = angular.fromJson(message);
            $scope.validator.messageFilePath = $scope.fileInfo.flowRelativePath;
        };

        $scope.successCert = function(message) {
            $scope.fileInfo = angular.fromJson(message);
            $scope.validator.certFilePath = $scope.fileInfo.flowRelativePath;
        };

        $scope.resetMessage = function() {
            $scope.fileInfo = {};
            $scope.validator.messageFilePath = "";
        };

        $scope.resetCert = function() {
            $scope.fileInfo = {};
            $scope.validator.certFilePath = "";
        };

		$scope.copyCcdaEmail = function(ccda, domain) {
			return ccda + "@" + domain;
		};

        $scope.validate = function() {
            $scope.laddaLoading = true;
            MessageValidatorFactory.save($scope.validator, function(data) {
                $scope.laddaLoading = false;
                $state.go('direct.report', {message_id: data.messageId});
            }, function(data) {
                $scope.laddaLoading = false;
                throw {
                    code: data.data.code,
                    url: data.data.url,
                    message: data.data.message
                };
            });
        };

    }
]);
