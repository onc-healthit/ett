var ccdaValidator = angular.module('ttt.direct.ccdar2Validator', []);

ccdaValidator.controller('CCDAR2ValidatorCtrl', ['$scope', 'CCDAR2ValidatorFactory', '$state', 'ApiUrl', 'CCDAR21Documents', 'CCDADocumentsFactory',
    function($scope, CCDAR2ValidatorFactory, $state, ApiUrl, CCDAR21Documents, CCDADocumentsFactory) {

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

        $scope.sutRole = "sender";

        $scope.ccdaData = {};
        CCDAR21Documents.getCcdaDocuments(function(data) {
            $scope.ccdaDataSender = data.data.sender;
            // $scope.ccdaData = $scope.ccdaDataSender;
            $scope.ccdaDataReceiver = data.data.receiver;
        });

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

        $scope.changed = function(item) {
            $scope.type = item;
        };

        $scope.apiUrl = ApiUrl.get();

        $scope.validator = {
            "messageFilePath": "",
            "validationObjective": "",
            "referenceFileName": ""
        };

        $scope.successMessage = function(message) {
            $scope.fileInfo = angular.fromJson(message);
            $scope.validator.messageFilePath = $scope.fileInfo.flowRelativePath;
        };

        $scope.resetMessage = function() {
            $scope.fileInfo = {};
            $scope.validator.messageFilePath = "";
        };

        $scope.validate = function() {
            $scope.laddaLoading = true;
            if ($scope.ccdaDocument) {
                if ($scope.ccdaDocument.name && $scope.ccdaDocument.path) {
                    $scope.validator.validationObjective = $scope.ccdaDocument.path[$scope.ccdaDocument.path.length - 1];
                    $scope.validator.referenceFileName = $scope.ccdaDocument.name;
                    CCDAR2ValidatorFactory.save($scope.validator, function(data) {
                        $scope.laddaLoading = false;
                        $scope.ccdaResult = data;
                    }, function(data) {
                        $scope.laddaLoading = false;
                        throw {
                            code: data.data.code,
                            url: data.data.url,
                            message: data.data.message
                        };
                    });
                }  else {
                    $scope.laddaLoading = false;
                    throw {
                        code: "No code",
                        url: "",
                        message: "You need to select a C-CDA document name"
                    };
                }
            } else {
                $scope.laddaLoading = false;
                throw {
                    code: "No code",
                    url: "",
                    message: "You need to select a C-CDA document type"
                };
            }
        };

        $scope.$watch('sutRole', function(newV, oldV) {

        });

    }
]);
